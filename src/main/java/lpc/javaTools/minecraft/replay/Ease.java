package lpc.javaTools.minecraft.replay;

public interface Ease {
	// 输入输出都应该是(0,1)之间的数
	double translate(double val);
	default Ease then(Ease next) {
		return v->next.translate(translate(v));
	}
}
