package com.draft.restapi.common.aspect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.draft.restapi.service.DummyTestService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
class MethodArgumentCaptureAspectTest {

    private DummyTestService proxyService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DummyTestService target = new DummyTestService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        MethodArgumentCaptureAspect aspect = new MethodArgumentCaptureAspect();
        factory.addAspect(aspect);
        proxyService = factory.getProxy();
    }

    @Test
    void testCapture_methodWithoutArgs() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.methodWithoutArgs();
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertEquals("DummyTestService.methodWithoutArgs()", capturedMethods.get(0).get("__method__"));
    }

    @Test
    void testCapture_methodWithArgs() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.methodWithArgs("var1", null);
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertEquals("DummyTestService.methodWithArgs(..)", capturedMethods.get(0).get("__method__"));
        assertEquals("var1", capturedMethods.get(0).get("strArg"));
        assertEquals("null", capturedMethods.get(0).get("intArg"));
    }

    @Test
    void testCapture_methodWithArrayArg() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.methodWithArrayArg(new String[]{"var1", "var2"});
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertEquals("DummyTestService.methodWithArrayArg(..)", capturedMethods.get(0).get("__method__"));
        assertEquals("{var1, var2}", capturedMethods.get(0).get("args"));
    }

    @Test
    void testCapture_methodWithObjectArrayArg() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.methodWithObjectArrayArg(new Object[]{"var1", 2});
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertEquals("DummyTestService.methodWithObjectArrayArg(..)", capturedMethods.get(0).get("__method__"));
        assertEquals("{var1, 2}", capturedMethods.get(0).get("args"));
    }

    @Test
    void testCapture_methodWithPrimitiveArrayArg() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.methodWithPrimitiveArrayArg(new int[]{1, 2, 3});
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertEquals("DummyTestService.methodWithPrimitiveArrayArg(..)", capturedMethods.get(0).get("__method__"));
        assertEquals("{1, 2, 3}", capturedMethods.get(0).get("args"));
    }

    @Test
    void testCapture_methodWithVarArgs() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.methodWithVarArgs("var1", "var2");
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertEquals("DummyTestService.methodWithVarArgs(..)", capturedMethods.get(0).get("__method__"));
        assertEquals("{var1, var2}", capturedMethods.get(0).get("args"));
    }

    @Test
    void testCapture_innerMethod() {
        assertThrows(IllegalArgumentException.class, () -> {
            proxyService.outerMethod(); // innerMethod throws exception
        });
        List<Map<String, String>> capturedMethods = (List<Map<String, String>>) request.getAttribute("capturedMethodArgs");
        assertNotEquals("DummyTestService.innerMethod()", capturedMethods.get(0).get("__method__"));
        assertEquals("DummyTestService.outerMethod()", capturedMethods.get(0).get("__method__"));
    }
}
