package com.cht.demo.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.ArrayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 支援型別轉換的複製工具
 * 
 * @author rickwang
 *
 */
@Slf4j
public class Beans {

	/**
	 * 不會複製 src 中 properties 為 null 到 dst 內
	 * 
	 * 不會複製 dst 不存在的 properties
	 * 
	 * @param <D>
	 * @param src	來源物件
	 * @param dst	要被複製欄位的物件
	 * @param properties	要複製哪些欄位，若不給就是全部複製
	 * 
	 * @return	回傳 dst
	 */
	public static <D> D copy(Object src, D dst, String... properties) {

		preprocess();

		try {
			for (var pd : BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptors(src)) {
				var name = pd.getName();
				// 不指定 properties 白名單，就代表全部都要
				if ((properties.length == 0) || (ArrayUtils.indexOf(properties, name) >= 0)) {
					var value = pd.getReadMethod().invoke(src); // 從 src 讀出來
					if ((value != null) || isRequiredField(pd)) {

						if (value == null) {
							BeanUtils.copyProperty(dst, name, null);

						} else {
							BeanUtils.setProperty(dst, name, value);
						}

						// BeanUtils.setProperty(dst, name, value); // 寫到 dst 內，這裡支援型別轉換，如果 property 不存在也沒有關係
						// BeanUtils.copyProperty(dst, name, value); // 改用 copyProperty 方法，支援複製 null 值
					}
				}
			}

			return dst;

		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}
	
	public static <D> D copy(Map<?, ?> src, D dst, String... properties) {

		preprocess();

		try {
			for (var pd : BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptors(dst)) {
				var name = pd.getName();
				// 不指定 properties 白名單，就代表全部都要
				if ((properties.length == 0) || (ArrayUtils.indexOf(properties, name) >= 0)) {
					var value = src.get(name); // 從 src 讀出來
					if ((value != null) || isRequiredField(pd)) { // 針對 有宣告@RequiredField 或是不為null
						// BeanUtils.setProperty(dst, name, value); // 寫到 dst 內，這裡支援型別轉換，如果 property 不存在也沒有關係
						// BeanUtils.copyProperty(dst, name, value); // 改用 copyProperty 方法，支援複製 null 值

						if (value == null) {
							BeanUtils.copyProperty(dst, name, null);

						} else {
							BeanUtils.setProperty(dst, name, value);
						}
					}
				}
			}

			return dst;

		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(Object bean, String name) {
		try {
			return (T) BeanUtils.getProperty(bean, name);
			
		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getArrayProperty(Object bean, String name) {
		try {
			return (T) BeanUtils.getArrayProperty(bean, name);
			
		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}
	
	public static <T> T getProperty(Object bean, Field field) {
		return getProperty(bean, field.getName());
	}
	
	public static <T> T getArrayProperty(Object bean, Field field) {
		return getArrayProperty(bean, field.getName());
	}
	
	/**
	 * 建立一個物件副本
	 * 
	 * @param <T>
	 * @param src
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T clone(T src) {
		preprocess();
		
		try {		
			var dst = (T) src.getClass().getConstructor().newInstance();
			return copy(src, dst);
			
		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}

	/**
	 * 以 src 為基準，判斷 src 不為 null 的 properties 且與 dst 對應 properties 是否相同？
	 * 
	 * @param <T>
	 * @param src
	 * @param dst
	 * @param ignoreProperties
	 * @return
	 */
	public static <T> boolean equal(T src, T dst, String... ignoreProperties) {
		preprocess();
		
		try {		
			for (var pd : BeanUtilsBean.getInstance().getPropertyUtils().getPropertyDescriptors(src)) {
				
				// 濾除不要比對的 property
				var name = pd.getName();
				if (ArrayUtils.indexOf(ignoreProperties, name) >= 0) {
					continue;
				}

					var method = pd.getReadMethod();

					var a = method.invoke(src);
					if ((a != null) || isRequiredField(pd)) { // 只判斷 src 內非 null 的 property 或是有宣告@RequiredField
						var b = method.invoke(dst);
						
						if ((a == null) && (b == null)) {
							continue;							
						}	
						
						if ((a == null) && (b != null)) {// 若 src 無值，但 dst 無值，代表是RequiredField且有差異
							return false;					
						}	
						
						if (b == null) { // 若 src 有值，但 dst 無值，代表有差異了
							return false;
						}

						// 兩者內容不同
						if (a.equals(b) == false) {
							return false;
						}
					}
				}
			
			return true;
			
		} catch (Exception e) {
			throw new ConversionException(e);
		}
	}
	
	/**
	 * 註冊 Instant, LocalDateTime, LocalDate 的轉換工具到 BeanUtils 中
	 */
	
	static final DateTimeConverter dateTimeConverter = new DateTimeConverter();
	
	static synchronized void preprocess() {
		var converters = BeanUtilsBean.getInstance().getConvertUtils();
		if (converters.lookup(Instant.class) == null) {
			log.info("Register DateTimeConverter for Instant, LocalDateTime, LocalDate in {}",
					Beans.class.getClassLoader());
			
			converters.register(dateTimeConverter, Instant.class);
			converters.register(dateTimeConverter, LocalDateTime.class);
			converters.register(dateTimeConverter, LocalDate.class);	
		}		
	}
	
	static class DateTimeConverter implements Converter {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Class<T> type, Object value) {
			if (value == null) {
				return null;
			}
			
			if (type == value.getClass()) {
				return (T) value;
			}				
			
			if (type == Instant.class) {
				if (value instanceof LocalDateTime ldt) {
					return (T) DateUtils.toInstant(ldt);
					
				} else if (value instanceof LocalDate ld) {
					return (T) DateUtils.toInstant(ld);					
				}
				
			} else if (type == LocalDateTime.class) {
				if (value instanceof Instant i) {
					return (T) DateUtils.toLocalDateTime(i);
					
				} else if (value instanceof Timestamp t) {
					return (T) t.toLocalDateTime();
				}
				
			} else if (type == LocalDate.class) {
				if (value instanceof Instant i) {
					return (T) DateUtils.toLocalDate(i);
					
				} else if (value instanceof Timestamp t) {
					return (T) t.toLocalDateTime().toLocalDate();
				} else if (value instanceof Date t) {
					return (T) t.toLocalDate();
				}
			}
			
			throw new ConversionException(String.format("Cannot convert %s to %s", value, type));
		}	
	}
	
	private static boolean isRequiredField(PropertyDescriptor pd) throws NoSuchFieldException, SecurityException {
		return false;
////		var field = clazz.getField(pd.getName());		
////		return field.isAnnotationPresent(RequiredField.class);
//		
//		var readMethod = pd.getReadMethod();
//		if (readMethod != null) {
//	        try {
//	            var field = readMethod.getDeclaringClass().getDeclaredField(pd.getName());
//	            return field.isAnnotationPresent(RequiredField.class);
//	        } catch (NoSuchFieldException e) {
//	            // 如果找不到對應的 Field 對象，則該屬性不是必填屬性
//	            return false;
//	        }
//		}
//		
//		return false;
//		
////	    var writeMethod = pd.getWriteMethod();
////	    
////	    if (readMethod != null && writeMethod != null) {
////	        var field = readMethod.getDeclaringClass().getDeclaredField(pd.getName());
////	        return field.isAnnotationPresent(RequiredField.class);
////	    }
////	    return false;
	}

}
