package com.portfolio.insurance.validation;

public final class DocumentValidator {

    private DocumentValidator() {
    }

    public static boolean isCpf(String value) {
        if (value == null || !value.matches("\\d{11}") || hasAllRepeatedDigits(value)) {
            return false;
        }
        int firstDigit = calculateCpfDigit(value, 9);
        int secondDigit = calculateCpfDigit(value, 10);
        return firstDigit == Character.digit(value.charAt(9), 10)
                && secondDigit == Character.digit(value.charAt(10), 10);
    }

    public static boolean isCnpj(String value) {
        if (value == null || !value.matches("\\d{14}") || hasAllRepeatedDigits(value)) {
            return false;
        }
        int firstDigit = calculateCnpjDigit(value, 12);
        int secondDigit = calculateCnpjDigit(value, 13);
        return firstDigit == Character.digit(value.charAt(12), 10)
                && secondDigit == Character.digit(value.charAt(13), 10);
    }

    private static boolean hasAllRepeatedDigits(String value) {
        char first = value.charAt(0);
        for (int i = 1; i < value.length(); i++) {
            if (value.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private static int calculateCpfDigit(String value, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.digit(value.charAt(i), 10) * (length + 1 - i);
        }
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }

    private static int calculateCnpjDigit(String value, int length) {
        int[] firstWeights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] secondWeights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights = length == 12 ? firstWeights : secondWeights;

        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.digit(value.charAt(i), 10) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
