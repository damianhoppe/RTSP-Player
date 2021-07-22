package pl.huczeq.rtspplayer.domain.cameragenerator;

import org.junit.Test;

import pl.huczeq.rtspplayer.domain.cameragenerator.exceptions.ParsingException;
import pl.huczeq.rtspplayer.domain.cameragenerator.expression.ProcessedVariable;

import static org.junit.Assert.*;

public class ProcessedVariableTest {

    @Test
    public void ProcessedVariable_parsing_1() {
        String data = "1,2,3";
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", data);
            assertEquals(3, ProcessedVariable.getValues().size());
        }catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void ProcessedVariable_parsing_1_values() {
        String data = "1,2,3";
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", data);
            assertEquals("1", ProcessedVariable.getValues().get(0));
            assertEquals("2", ProcessedVariable.getValues().get(1));
            assertEquals("3", ProcessedVariable.getValues().get(2));
        }catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void ProcessedVariable_parsing_2() {
        String data = "1-200";
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", data);
            assertEquals(200, ProcessedVariable.getValues().size());
        }catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void ProcessedVariable_parsing_3() {
        String data = "10/20";
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", data);
            assertEquals(20, ProcessedVariable.getValues().size());
        }catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void ProcessedVariable_parsing_overload() {
        //Values 1-4 in 3 modes together
        String data = "1,2,3,4,1-4,1/4";
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", data);
            assertEquals(4, ProcessedVariable.getValues().size());
        }catch (ParsingException e) {
            fail();
        }
    }

    @Test
    public void ProcessedVariable_parsing_all() {
        String data = "1,2,3-10,11/10";
        ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", data);
        assertEquals(20, ProcessedVariable.getValues().size());
    }

    @Test
    public void ProcessedVariable_parsing_exceptions() {
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "1-");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "-3");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "10-2");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "10/");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "/2");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "-1");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "-1,-2");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        try {
            ProcessedVariable ProcessedVariable = new ProcessedVariable("Test", "-1/-2");
            ProcessedVariable.parse();
            fail();
        }catch (ParsingException e) {
        }
        assertTrue(true);
    }
}