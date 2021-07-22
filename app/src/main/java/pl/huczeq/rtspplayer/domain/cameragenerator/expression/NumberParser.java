package pl.huczeq.rtspplayer.domain.cameragenerator.expression;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.domain.cameragenerator.exceptions.ParsingException;

public class NumberParser {

    /**
     * Parses text containing values or a range of them into a list of single values
     * e.g.:
     * "1,2,3,3" -> {"1", "2", "3"}
     * "1,2-4,3" -> {"1", "2", "3", "4"}
     * "1,2/5,3" -> {"1", "2", "3", "4", "5", "6"}
     * @param text - input text
     * @return output values
     */
    public static List<String> parse(String text) {
        List<String> result = new ArrayList<>();
        parseText(text, result);
        return result;
    }

    private static void parseText(String text, List<String> values) {
        if(text.isEmpty())
            throw new ParsingException(ParsingException.Error.DATA_EMPTY);
        String[] dataUnits = text.split(",");
        for(String dataUnit : dataUnits) {
            int firstValue;
            int secondValue;
            if(dataUnit.contains("-")) {
                String[] dataArray = dataUnit.split("-");
                if(dataArray.length != 2)
                    throw new ParsingException(ParsingException.Error.NUMBER_INTERVAL_ARRAY_ERROR);
                try {
                    firstValue = Integer.parseInt(dataArray[0]);
                }catch (NumberFormatException e) {
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                try {
                    secondValue = Integer.parseInt(dataArray[1]);
                }catch (NumberFormatException e) {
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                if(firstValue < 0 || secondValue < 0) {
                    throw new ParsingException(ParsingException.Error.NEGATIVE_NUMBER_ERROR);
                }
                if(firstValue > secondValue)
                    throw new ParsingException(ParsingException.Error.NUMBER_INTERVAL_ORDER_ERROR);
                for(int i = firstValue; i <= secondValue; i++) {
                    add(String.valueOf(i), values);
                }
            }else if(dataUnit.contains("/")) {
                String[] dataArray = dataUnit.split("/");
                if(dataArray.length != 2)
                    throw new ParsingException(ParsingException.Error.FIRST_LENGHT_ARRAY_ERROR);
                try {
                    firstValue = Integer.parseInt(dataArray[0]);
                }catch (NumberFormatException e) {
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                try {
                    secondValue = Integer.parseInt(dataArray[1]);
                }catch (NumberFormatException e) {
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                if(firstValue < 0 || secondValue < 0) {
                    throw new ParsingException(ParsingException.Error.NEGATIVE_NUMBER_ERROR);
                }
                for(int i = firstValue; i < firstValue+secondValue; i++) {
                    add(String.valueOf(i), values);
                }
            }else {
                try {
                    firstValue = Integer.parseInt(dataUnit);
                }catch (NumberFormatException e) {
                    throw new ParsingException(ParsingException.Error.FORMAT_NUMBER_ERROR);
                }
                if(firstValue < 0) {
                    throw new ParsingException(ParsingException.Error.NEGATIVE_NUMBER_ERROR);
                }
                add(String.valueOf(firstValue), values);
            }
        }
    }

    private static void add(String value, List<String> list) {
        if(list.contains(value)) {
            return;
        }
        list.add(value);
    }
}
