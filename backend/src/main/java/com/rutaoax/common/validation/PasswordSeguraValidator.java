package com.rutaoax.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordSeguraValidator implements ConstraintValidator<PasswordSegura, String> {

    private static final Pattern PATRON = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        return valor == null || PATRON.matcher(valor).matches();
    }
}
