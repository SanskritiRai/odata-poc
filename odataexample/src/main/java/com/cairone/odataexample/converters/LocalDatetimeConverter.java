package com.cairone.odataexample.converters;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.odataexample.utils.FechaUtil;

@Converter(autoApply = true)
public class LocalDatetimeConverter implements AttributeConverter<LocalDateTime, Date> {

	@Override
	public Date convertToDatabaseColumn(LocalDateTime attribute) {
		return FechaUtil.asDate(attribute);
	}

	@Override
	public LocalDateTime convertToEntityAttribute(Date dbData) {
		return FechaUtil.asLocalDateTime(dbData);
	}
}
