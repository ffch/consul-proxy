package cn.pomit.consul.http.res;

public enum ResCode {
	NOT_FOUND(404), OK(200), REDIRECT(302), INTERNAL_ERROR(500);
	int value = 200;

	ResCode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
