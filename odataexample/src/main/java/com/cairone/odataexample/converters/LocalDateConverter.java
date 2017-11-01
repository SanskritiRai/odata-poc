package com.cairone.odataexample.converters;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.cairone.odataexample.utils.FechaUtil;

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

	@Override
	public Date convertToDatabaseColumn(LocalDate localDate) {
		return FechaUtil.asDate(localDate);
	}

	@Override
	public LocalDate convertToEntityAttribute(Date date) {
		return FechaUtil.asLocalDate(date);
	}
}
