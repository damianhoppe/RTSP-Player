package pl.huczeq.rtspplayer;

import org.junit.Test;

import pl.huczeq.rtspplayer.data.expression.Variable;
import pl.huczeq.rtspplayer.exceptions.ParsingException;

import static org.junit.Assert.*;

public class VariableTest {

    @Test
    public void variable_parsing_1() {
        String data = "1,2,3";
        try {
            Variable variable = new Variable("Test", data);
            assertEquals(3, variable.getValues().size());
        }catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void variable_parsing_2() {
        String data = "1-200";
        try {
            Variable variable = new Variable("Test", data);
            assertEquals(200, variable.getValues().size());
        }catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void variable_parsing_3() {
        String data = "10/20";
        try {
            Variable variable = new Variable("Test", data);
            assertEquals(20, variable.getValues().size());
        }catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void variable_parsing_overload() {
        String data = "1,2,3,4,1-4,1/4";
        try {
            Variable variable = new Variable("Test", data);
            assertEquals(4, variable.getValues().size());
        }catch (ParsingException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void variable_parsing_all() {
        String data = "1,2,3-10,11/10";
        Variable variable = new Variable("Test", data);
        assertEquals(20, variable.getValues().size());
    }

    @Test
    public void variable_parsing_exceptions() {
        try {
            Variable variable = new Variable("Test", "");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "1-");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "-3");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "10-2");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "10/");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "/2");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "-1");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "-1,-2");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        try {
            Variable variable = new Variable("Test", "-1/-2");
            variable.parse();
            fail();
        }catch (ParsingException e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }
}