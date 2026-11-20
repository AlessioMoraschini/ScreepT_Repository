package gui.texteditor.commons.filesearch.content;

/**
 * Speed presets of the content search: more threads make it faster but use more CPU, memory and disk bandwidth.
 * Every preset is a share of the machine it runs on (cores, memory), not a fixed number.
 */
public enum SearchPerformance {

	/** about 1/8 of the cores, lowest priority */
	ECO("fsearch.perf_eco", 1, 8, Thread.NORM_PRIORITY - 2),
	/** half of the cores */
	BALANCED("fsearch.perf_balanced", 1, 2, Thread.NORM_PRIORITY - 1),
	/** three quarters of the cores */
	FAST("fsearch.perf_fast", 3, 4, Thread.NORM_PRIORITY),
	/** every core */
	MAXIMUM("fsearch.perf_max", 1, 1, Thread.NORM_PRIORITY);

	/** I18n id of the label */
	public final String labelId;
	public final int threadPriority;
	private final int shareNumerator;
	private final int shareDenominator;

	SearchPerformance(String labelId, int shareNumerator, int shareDenominator, int threadPriority) {
		this.labelId = labelId;
		this.shareNumerator = shareNumerator;
		this.shareDenominator = shareDenominator;
		this.threadPriority = threadPriority;
	}

	/**
	 * Read block size: small enough to stay in the CPU cache while it is scanned
	 * (bigger blocks measured slower), smaller when the JVM has little memory.
	 */
	public int chunkBytes() {
		return Runtime.getRuntime().maxMemory() < 768L * 1024 * 1024 ? 256 * 1024 : 512 * 1024;
	}

	/** Worker threads: this preset's share of the cores, limited by the memory the blocks need */
	public int threads(int cores) {
		int t = Math.max(this == ECO ? 1 : 2, cores * shareNumerator / shareDenominator);
		long memoryLimit = Runtime.getRuntime().maxMemory() / 4 / (chunkBytes() * 6L);
		return (int) Math.max(1, Math.min(t, memoryLimit));
	}

	public static SearchPerformance fromName(String name) {
		for (SearchPerformance p : values()) {
			if (p.name().equalsIgnoreCase(name))
				return p;
		}
		return BALANCED;
	}
}
