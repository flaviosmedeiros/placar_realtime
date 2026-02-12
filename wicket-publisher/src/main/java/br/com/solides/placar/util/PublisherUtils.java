package br.com.solides.placar.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;


public class PublisherUtils {
	
	private PublisherUtils() {}	
	
	
	public static boolean nuloOuVazio(String arg) {
		return StringUtils.isEmpty(arg);
	}

	public static boolean nuloOuVazio(Integer arg) {
		return (Objects.isNull(arg) || arg == 0);
	}

	public static boolean nuloOuVazio(Long arg) {
		return (Objects.isNull(arg) || arg == 0);
	}

	public static <T extends Object> boolean nuloOuVazio(Collection<T> arg) {
		return (Objects.isNull(arg) || arg.isEmpty());
	}

	public static boolean nuloOuVazio(Map<?, ?> arg) {
		return ObjectUtils.isEmpty(arg);
	}

	public static boolean nuloOuVazio(Object arg) {
		return Objects.isNull(arg);
	}

}
