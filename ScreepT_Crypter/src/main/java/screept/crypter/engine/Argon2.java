/**
 *
 * =========================================================================================
 *  Copyright (C) 2019-2020
 *
 *  AM-Design-Development - (Alessio Moraschini) - All Rights Reserved
 * =========================================================================================
 *
 * You should have received a copy of the license with this file.
 * If not, please write to: info@am-design-development.com, or visit : https://www.am-design-development.com
 */
package screept.crypter.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Argon2 version 0x13 (RFC 9106), pure Java on top of {@link Blake2b}. Lanes are filled in parallel.
 * The cost stored in a file header packs the parameters: {@code lanes << 16 | passes << 8 | log2(memory KiB)}.
 */
final class Argon2 {

	static final int TYPE_D = 0;
	static final int TYPE_I = 1;
	static final int TYPE_ID = 2;

	private static final int VERSION = 0x13;
	private static final int BLOCK_LONGS = 128;
	private static final int SYNC_POINTS = 4;

	private final int type;
	private final int passes;
	private final int lanes;
	private final int laneLength;
	private final int segmentLength;
	private final int blockCount;
	private final long[] memory;

	private Argon2(int type, int passes, int memoryKiB, int lanes) {
		this.type = type;
		this.passes = passes;
		this.lanes = lanes;
		int m = Math.max(memoryKiB, 8 * lanes);
		this.segmentLength = m / (lanes * SYNC_POINTS);
		this.laneLength = segmentLength * SYNC_POINTS;
		this.blockCount = laneLength * lanes;
		this.memory = new long[blockCount * BLOCK_LONGS];
	}

	// ---------------------------------------------------------------- cost packing

	static int cost(int memoryLog2KiB, int passes, int lanes) {
		return lanes << 16 | passes << 8 | memoryLog2KiB;
	}

	static int memoryLog2(int cost) {
		return cost & 0xff;
	}

	static int passes(int cost) {
		return (cost >>> 8) & 0xff;
	}

	static int lanes(int cost) {
		return (cost >>> 16) & 0xff;
	}

	// ---------------------------------------------------------------- API

	static byte[] hash(int type, byte[] password, byte[] salt, byte[] secret, byte[] ad, int passes, int memoryKiB, int lanes,
			int outLen) {
		if (passes < 1 || lanes < 1 || lanes > 0xffffff || outLen < 4 || memoryKiB < 8 * lanes) {
			throw new IllegalArgumentException("Invalid Argon2 parameters");
		}
		Argon2 a = new Argon2(type, passes, memoryKiB, lanes);
		try {
			return a.run(password, salt, secret == null ? new byte[0] : secret, ad == null ? new byte[0] : ad, memoryKiB, outLen);
		} finally {
			Arrays.fill(a.memory, 0);
		}
	}

	private byte[] run(byte[] pwd, byte[] salt, byte[] secret, byte[] ad, int memoryKiB, int outLen) {
		byte[] h0 = new Blake2b(64).updateLE32(lanes).updateLE32(outLen).updateLE32(memoryKiB).updateLE32(passes)
				.updateLE32(VERSION).updateLE32(type).updateLE32(pwd.length).update(pwd).updateLE32(salt.length).update(salt)
				.updateLE32(secret.length).update(secret).updateLE32(ad.length).update(ad).digest();
		byte[] seed = Arrays.copyOf(h0, 72);
		for (int l = 0; l < lanes; l++) {
			for (int c = 0; c < 2; c++) {
				le32(seed, 64, c);
				le32(seed, 68, l);
				byte[] block = hPrime(seed, 1024);
				int base = (l * laneLength + c) * BLOCK_LONGS;
				for (int k = 0; k < BLOCK_LONGS; k++) {
					memory[base + k] = Blake2b.le64(block, k * 8);
				}
				Arrays.fill(block, (byte) 0);
			}
		}
		Arrays.fill(seed, (byte) 0);
		Arrays.fill(h0, (byte) 0);

		fillMemory();

		long[] last = new long[BLOCK_LONGS];
		for (int l = 0; l < lanes; l++) {
			int base = (l * laneLength + laneLength - 1) * BLOCK_LONGS;
			for (int k = 0; k < BLOCK_LONGS; k++) {
				last[k] ^= memory[base + k];
			}
		}
		byte[] c = new byte[1024];
		for (int k = 0; k < BLOCK_LONGS; k++) {
			le64(c, k * 8, last[k]);
		}
		try {
			return hPrime(c, outLen);
		} finally {
			Arrays.fill(c, (byte) 0);
			Arrays.fill(last, 0);
		}
	}

	private void fillMemory() {
		ExecutorService pool = lanes > 1 ? Executors.newFixedThreadPool(Math.min(lanes, Runtime.getRuntime().availableProcessors()),
				r -> {
					Thread t = new Thread(r, "Argon2-lane");
					t.setDaemon(true);
					return t;
				}) : null;
		try {
			for (int pass = 0; pass < passes; pass++) {
				for (int slice = 0; slice < SYNC_POINTS; slice++) {
					if (pool == null) {
						fillSegment(pass, 0, slice);
						continue;
					}
					List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
					for (int l = 0; l < lanes; l++) {
						final int p = pass, lane = l, s = slice;
						tasks.add(() -> {
							fillSegment(p, lane, s);
							return null;
						});
					}
					for (Future<Void> f : pool.invokeAll(tasks)) {
						f.get();
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted");
		} catch (ExecutionException e) {
			throw new IllegalStateException("Argon2 failure", e.getCause());
		} finally {
			if (pool != null) {
				pool.shutdownNow();
			}
		}
	}

	private void fillSegment(int pass, int lane, int slice) {
		boolean independent = type == TYPE_I || (type == TYPE_ID && pass == 0 && slice < SYNC_POINTS / 2);
		long[] address = null, input = null, zero = null;
		long[] r = new long[BLOCK_LONGS], tmp = new long[BLOCK_LONGS];
		if (independent) {
			address = new long[BLOCK_LONGS];
			input = new long[BLOCK_LONGS];
			zero = new long[BLOCK_LONGS];
			input[0] = pass;
			input[1] = lane;
			input[2] = slice;
			input[3] = blockCount;
			input[4] = passes;
			input[5] = type;
		}
		int start = 0;
		if (pass == 0 && slice == 0) {
			start = 2;
			if (independent) {
				nextAddresses(address, input, zero, r, tmp);
			}
		}
		int curr = lane * laneLength + slice * segmentLength + start;
		int prev = curr % laneLength == 0 ? curr + laneLength - 1 : curr - 1;
		for (int i = start; i < segmentLength; i++, curr++, prev++) {
			if (curr % laneLength == 1) {
				prev = curr - 1;
			}
			long rand;
			if (independent) {
				if (i % BLOCK_LONGS == 0) {
					nextAddresses(address, input, zero, r, tmp);
				}
				rand = address[i % BLOCK_LONGS];
			} else {
				rand = memory[prev * BLOCK_LONGS];
			}
			int refLane = (int) ((rand >>> 32) % lanes);
			if (pass == 0 && slice == 0) {
				refLane = lane;
			}
			int refIndex = indexAlpha(pass, slice, i, rand & 0xffffffffL, refLane == lane);
			fillBlock(memory, prev * BLOCK_LONGS, memory, (refLane * laneLength + refIndex) * BLOCK_LONGS, memory, curr * BLOCK_LONGS,
					pass != 0, r, tmp);
		}
		Arrays.fill(r, 0);
		Arrays.fill(tmp, 0);
		if (address != null) {
			Arrays.fill(address, 0);
			Arrays.fill(input, 0);
		}
	}

	private int indexAlpha(int pass, int slice, int index, long rand32, boolean sameLane) {
		long area;
		if (pass == 0) {
			if (slice == 0) {
				area = index - 1;
			} else if (sameLane) {
				area = (long) slice * segmentLength + index - 1;
			} else {
				area = (long) slice * segmentLength + (index == 0 ? -1 : 0);
			}
		} else if (sameLane) {
			area = laneLength - segmentLength + index - 1;
		} else {
			area = laneLength - segmentLength + (index == 0 ? -1 : 0);
		}
		long rel = (rand32 * rand32) >>> 32;
		rel = area - 1 - ((area * rel) >>> 32);
		long startPos = 0;
		if (pass != 0) {
			startPos = slice == SYNC_POINTS - 1 ? 0 : (long) (slice + 1) * segmentLength;
		}
		return (int) ((startPos + rel) % laneLength);
	}

	private static void nextAddresses(long[] address, long[] input, long[] zero, long[] r, long[] tmp) {
		input[6]++;
		fillBlock(zero, 0, input, 0, address, 0, false, r, tmp);
		fillBlock(zero, 0, address, 0, address, 0, false, r, tmp);
	}

	/** next = G(prev XOR ref) [XOR next when withXor]. Arrays and offsets in longs. */
	private static void fillBlock(long[] pa, int po, long[] ra, int ro, long[] na, int no, boolean withXor, long[] r, long[] tmp) {
		for (int k = 0; k < BLOCK_LONGS; k++) {
			r[k] = ra[ro + k] ^ pa[po + k];
		}
		if (withXor) {
			for (int k = 0; k < BLOCK_LONGS; k++) {
				tmp[k] = r[k] ^ na[no + k];
			}
		} else {
			System.arraycopy(r, 0, tmp, 0, BLOCK_LONGS);
		}
		for (int i = 0; i < 8; i++) {
			int b = 16 * i;
			round(r, b, b + 1, b + 2, b + 3, b + 4, b + 5, b + 6, b + 7, b + 8, b + 9, b + 10, b + 11, b + 12, b + 13, b + 14, b + 15);
		}
		for (int i = 0; i < 8; i++) {
			int b = 2 * i;
			round(r, b, b + 1, b + 16, b + 17, b + 32, b + 33, b + 48, b + 49, b + 64, b + 65, b + 80, b + 81, b + 96, b + 97, b + 112,
					b + 113);
		}
		for (int k = 0; k < BLOCK_LONGS; k++) {
			na[no + k] = tmp[k] ^ r[k];
		}
	}

	private static void round(long[] v, int v0, int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9, int v10,
			int v11, int v12, int v13, int v14, int v15) {
		g(v, v0, v4, v8, v12);
		g(v, v1, v5, v9, v13);
		g(v, v2, v6, v10, v14);
		g(v, v3, v7, v11, v15);
		g(v, v0, v5, v10, v15);
		g(v, v1, v6, v11, v12);
		g(v, v2, v7, v8, v13);
		g(v, v3, v4, v9, v14);
	}

	private static void g(long[] v, int a, int b, int c, int d) {
		v[a] = blaMka(v[a], v[b]);
		v[d] = Long.rotateRight(v[d] ^ v[a], 32);
		v[c] = blaMka(v[c], v[d]);
		v[b] = Long.rotateRight(v[b] ^ v[c], 24);
		v[a] = blaMka(v[a], v[b]);
		v[d] = Long.rotateRight(v[d] ^ v[a], 16);
		v[c] = blaMka(v[c], v[d]);
		v[b] = Long.rotateRight(v[b] ^ v[c], 63);
	}

	private static long blaMka(long x, long y) {
		return x + y + 2 * (x & 0xffffffffL) * (y & 0xffffffffL);
	}

	/** Variable length hash H' of RFC 9106. */
	static byte[] hPrime(byte[] x, int outLen) {
		if (outLen <= 64) {
			return new Blake2b(outLen).updateLE32(outLen).update(x).digest();
		}
		byte[] out = new byte[outLen];
		byte[] v = new Blake2b(64).updateLE32(outLen).update(x).digest();
		System.arraycopy(v, 0, out, 0, 32);
		int pos = 32;
		int r = (outLen + 31) / 32 - 2;
		for (int i = 2; i <= r; i++) {
			v = new Blake2b(64).update(v).digest();
			System.arraycopy(v, 0, out, pos, 32);
			pos += 32;
		}
		byte[] lastV = new Blake2b(outLen - 32 * r).update(v).digest();
		System.arraycopy(lastV, 0, out, pos, lastV.length);
		return out;
	}

	private static void le32(byte[] b, int o, int x) {
		b[o] = (byte) x;
		b[o + 1] = (byte) (x >>> 8);
		b[o + 2] = (byte) (x >>> 16);
		b[o + 3] = (byte) (x >>> 24);
	}

	private static void le64(byte[] b, int o, long x) {
		for (int i = 0; i < 8; i++) {
			b[o + i] = (byte) (x >>> (8 * i));
		}
	}
}
