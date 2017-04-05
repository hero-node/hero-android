/**
 * BSD License
 * Copyright (c) Hero software.
 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.

 * Neither the name Hero nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hero.depandency;

import java.text.DecimalFormat;

/**
 * Created by xincai on 16-5-16.
 */
public class FormatUtils {
    public static final String FORMAT_MONEY = "###,###,##0.##";
    public static final DecimalFormat moneyFormatter = new DecimalFormat(FORMAT_MONEY);
    public static final String AREA_CODES = "010,020,021,022,023,024,025,027,028,029";

    // format a bank card number with "xxxx xxxx xxxx" style
    public static String formatBankCardString(String cardNumber) {
        if (cardNumber == null || cardNumber.length() == 0) {
            return "";
        }
        if (!cardNumber.matches("^[0-9 ]*$")) { // invalid
            return null;
        }
        if (cardNumber.matches("^(([\\d]{4} )*?[\\d]{0,4})$")) { // well formatted
            return null;
        }
        String tempString = cardNumber.replaceAll(" ", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tempString.length() / 4; i++) {
            String t = tempString.substring(i * 4, Math.min((i + 1) * 4, tempString.length()));
            sb.append(t);
            if (t.length() == 4) {
                sb.append(' ');
            }
        }
        sb.append(tempString.substring((tempString.length() / 4) * 4, tempString.length()));
        return sb.toString();
    }

    // format money style
    public static String formatMoneyString(String number) {
        if (number == null || number.length() == 0) {
            return "";
        }
        if (!number.matches("^[0-9,.]*$")) { // invalid
            return null;
        }
        if (number.matches("^([\\d]{1,3})((,[\\d]{3})*)(\\.(\\d){0,2})*$")) { // well formatted
            return null;
        }
        String tempString = number.replaceAll(",", "");
        double amount = Double.valueOf(tempString);
        String[] sp = tempString.split("\\.");
        if (sp.length > 1 && sp[1] != null && sp[1].length() > 2) {
            amount = Double.valueOf(tempString.substring(0, tempString.length() - (sp[1].length() - 2)));
        }
        return moneyFormatter.format(amount);
    }

    // format a fixed phone number with "010-12345678" format
    public static String formatFixNumberString(String number) {
        if (number == null || number.length() < 4 || number.length() > 5) {
            return null;
        }
        if (number.contains("-")) {
            return null;
        }

        boolean shouldFormat = false;
        int length = number.length();
        if (length == 4) {
            String prefix = number.substring(0, 3);
            if (AREA_CODES.contains(prefix)) {
                shouldFormat = true;
            }
        } else if (length == 5) {
            if (number.charAt(0) == '0') {
                shouldFormat = true;
            }
        }
        if (shouldFormat) {
            String tempString = number.substring(0, length - 1) + "-" + number.substring(length - 1, length);
            return tempString;
        }
        return null;
    }
}
