package pl.huczeq.rtspplayer;

import org.junit.Test;

import pl.huczeq.rtspplayer.data.expression.Variable;
import pl.huczeq.rtspplayer.exceptions.ParsingException;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
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
}