package function;

public class LoveCurve extends AbstractCurveFunction {
	private static final long serialVersionUID = 5824796222147322356L;

	public LoveCurve(double initialT, double interval, double finalT) {
		super(initialT, interval, finalT);
	}

	@Override
	public double updateX(double t) {
		return 16 * Math.pow(Math.sin(t), 3d);
	}

	@Override
	public double updateY(double t) {
		return 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
	}

}
