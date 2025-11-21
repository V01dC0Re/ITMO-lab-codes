package ru.itmo.lab3.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import java.math.BigDecimal;

@FacesConverter("bigDecimalConverter")
public class BigDecimalConverter implements Converter<BigDecimal> {

    @Override
    public BigDecimal getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new ConverterException(new jakarta.faces.application.FacesMessage(
                jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                "Ошибка", "Некорректное число: " + value
            ));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, BigDecimal value) {
        if (value == null) return "";
        return value.stripTrailingZeros().toPlainString();
    }
}