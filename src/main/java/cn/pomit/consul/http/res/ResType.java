package cn.pomit.consul.http.res;

public enum ResType {
	HTML("text/html"), JSON("application/json"), JS("application/javascript"), PNG("image/png"), TEXT(
			"text/plain"), JPG("image/jpg");
	String value = null;

	ResType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static ResType enumType(String value) {
		for (ResType m : ResType.values()) {
			if (m.getValue().equals(value))
				return m;
		}
		return HTML;
	}
}
