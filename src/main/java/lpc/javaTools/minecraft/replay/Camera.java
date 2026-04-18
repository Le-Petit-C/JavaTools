package lpc.javaTools.minecraft.replay;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.function.Consumer;

public class Camera {
	public final Vector3d position = new Vector3d(0, 0, 0);
	public final Quaterniond rotation = new Quaterniond();
	
	public Camera set(Camera camera) {
		position.set(camera.position);
		rotation.set(camera.rotation);
		return this;
	}
	
	/**
	 * 设置相机朝向目标点（类似gluLookAt）
	 * @param eye 相机位置
	 * @param center 目标点位置
	 * @param up 上方向向量（通常为 (0, 1, 0)）
	 */
	public Camera lookAt(Vector3d eye, Vector3d center, Vector3d up) {
		// 更新相机位置
		position.set(eye);
		
		// 创建视图矩阵
		Matrix4d lookAtMatrix = new Matrix4d().lookAt(eye, center, up);
		
		// 从视图矩阵提取旋转
		lookAtMatrix.getUnnormalizedRotation(rotation).invert();
		return this;
	}
	
	/**
	 * 从当前位置看向目标点
	 */
	public Camera lookAt(Vector3d target, Vector3d up) {
		return lookAt(position, target, up);
	}
	
	public Camera lookAtDirection(Vector3d targetDirection) {
		Vector3d currentForward = getForwardVector();
		rotation.premul(new Quaterniond().rotationTo(currentForward, targetDirection));
		return this;
	}
	/**
	 * 从当前位置看向目标点（进行最小球面转动）
	 */
	public Camera lookAt(Vector3d target) {
		return lookAtDirection(target.sub(position, new Vector3d()));
	}
	
	public Vector3d rotate(Vector3d vec, Vector3d dest) { return rotation.transform(vec, dest); }
	public Vector3d rotate(Vector3d vec) { return rotate(vec, vec); }
	
	public Camera applyRotate(Consumer<Quaterniond> rotator) {
		rotator.accept(rotation);
		return this;
	}
	
	/**
	 * 获取相机前向向量
	 */
	public Vector3d getForwardVector() {
		return getForwardVector(new Vector3d());
	}
	
	public Vector3d getForwardVector(Vector3d res) {
		return rotate(res.set(0, 0, -1));
	}
	
	/**
	 * 获取相机右向量
	 */
	public Vector3d getRightVector() {
		return getRightVector(new Vector3d());
	}
	
	public Vector3d getRightVector(Vector3d res) {
		return rotate(res.set(1, 0, 0));
	}
	
	/**
	 * 获取相机上向量
	 */
	public Vector3d getUpVector() {
		return getUpVector(new Vector3d());
	}
	
	public Vector3d getUpVector(Vector3d res) {
		return rotate(res.set(0, 1, 0));
	}
	
	/**
	 * 获取欧拉角（弧度）表示
	 * @return double数组 [yaw, pitch, roll]
	 */
	public Vector3d getEulerAngles() {
		Vector3d euler = new Vector3d();
		rotation.getEulerAnglesYXZ(euler);
		return euler;
	}
	
	public Vector3d getYawPitchRoll() {
		return getYawPitchRoll(new Vector3d());
	}
	
	public Vector3d getYawPitchRoll(Vector3d res) {
		// forward / up 向量
		Vector3d forward = getForwardVector();
		double yaw = Math.atan2(-forward.x, -forward.z);
		if(Double.isNaN(yaw)) yaw = 0;
		double pitch = Math.atan2(-forward.y, Math.sqrt(forward.x * forward.x + forward.z * forward.z));
		
		Vector3d rolledUp = getUpVector();
		Vector3d rolledRight = getRightVector();
		Vector3d unrolledRight = new Vector3d(1, 0, 0);
		unrolledRight.rotateY(yaw);
		
		double cosRoll = rolledRight.dot(unrolledRight);
		double sinRoll = rolledUp.dot(unrolledRight);
		double roll = Math.atan2(sinRoll, cosRoll);
		
		// --- 写入 ---
		res.x = yaw;
		res.y = pitch;
		res.z = roll;
		
		return res;
	}
	
	/**
	 * 从欧拉角设置旋转（弧度）
	 * @param yaw 偏航角（绕Y轴）
	 * @param pitch 俯仰角（绕X轴）
	 * @param roll 滚转角（绕Z轴）
	 */
	public void setEulerAngles(double yaw, double pitch, double roll) {
		rotation.identity()
			.rotateY(yaw)
			.rotateX(pitch)
			.rotateZ(roll);
	}
	
	/**
	 * 移动到指定位置并看向目标点
	 * @param position 相机位置
	 * @param target 目标点
	 */
	public void setPositionAndLookAt(Vector3d position, Vector3d target) {
		this.position.set(position);
		lookAt(target);
	}
	
	/**
	 * 沿当前朝向移动相机
	 * @param forwardDistance 前向移动距离
	 * @param rightDistance 右向移动距离
	 * @param upDistance 向上移动距离
	 */
	public void moveRelative(double forwardDistance, double rightDistance, double upDistance) {
		Vector3d moveVector = new Vector3d();
		getForwardVector().mul(forwardDistance, moveVector);
		getRightVector().mul(rightDistance).add(moveVector, moveVector);
		getUpVector().mul(upDistance).add(moveVector, moveVector);
		
		position.add(moveVector);
	}
	
	/**
	 * 旋转相机
	 * @param yawAngle 偏航角度（绕Y轴，弧度）
	 * @param pitchAngle 俯仰角度（绕X轴，弧度）
	 * @param rollAngle 滚转角度（绕Z轴，弧度）
	 */
	public void rotate(double yawAngle, double pitchAngle, double rollAngle) {
		Quaterniond temp = new Quaterniond();
		temp.rotationY(yawAngle).mul(rotation, rotation);
		temp.rotationX(pitchAngle).mul(rotation, rotation);
		temp.rotationZ(rollAngle).mul(rotation, rotation);
	}
	
	/**
	 * 获取位置向量
	 */
	public Vector3d getPosition() {
		return new Vector3d(position);
	}
	
	/**
	 * 设置位置
	 */
	public void setPosition(Vector3d position) {
		this.position.set(position);
	}
	
	/**
	 * 获取旋转四元数
	 */
	public Quaterniond getRotation() {
		return new Quaterniond(rotation);
	}
	
	/**
	 * 设置旋转
	 */
	public void setRotation(Quaterniond rotation) {
		this.rotation.set(rotation);
	}
	
	/**
	 * 创建视图矩阵（用于渲染）
	 */
	public Matrix4d getViewMatrix() {
		Matrix4d viewMatrix = new Matrix4d();
		
		// 创建旋转矩阵
		Matrix4d rotationMatrix = new Matrix4d().rotation(rotation);
		
		// 创建平移矩阵
		Matrix4d translationMatrix = new Matrix4d().translate(
			(float) -position.x,
			(float) -position.y,
			(float) -position.z
		);
		
		// 组合：先旋转，后平移
		return rotationMatrix.mul(translationMatrix, viewMatrix);
	}
	
	/**
	 * 创建投影矩阵（用于渲染）
	 * @param fov 视野角度（弧度）
	 * @param aspect 宽高比
	 * @param near 近平面
	 * @param far 远平面
	 */
	public Matrix4f getProjectionMatrix(float fov, float aspect, float near, float far) {
		return new Matrix4f().perspective(fov, aspect, near, far);
	}
	
	@Override
	public String toString() {
		Vector3d euler = getYawPitchRoll();
		return String.format("Camera[position=(%.2f, %.2f, %.2f), yaw=%.2f°, pitch=%.2f°, roll=%.2f°]",
			position.x, position.y, position.z,
			Math.toDegrees(euler.x), Math.toDegrees(euler.y), Math.toDegrees(euler.z));
	}
}