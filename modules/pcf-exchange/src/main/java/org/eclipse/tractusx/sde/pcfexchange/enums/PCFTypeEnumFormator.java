package org.eclipse.tractusx.sde.pcfexchange.enums;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

public class PCFTypeEnumFormator implements Formatter<PCFTypeEnum> {
	
    @Override
    public String print(PCFTypeEnum object, Locale locale) {
        return null;
    }

    @Override
    public PCFTypeEnum parse(String text, Locale locale) throws ParseException {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        return EnumUtils.getEnum(PCFTypeEnum.class, text.toUpperCase());
    }
}