package io.advantageous.qbit.example;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.jsend.JSendResponse;
import io.advantageous.qbit.jsend.JSendResponseBuilder;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;

@RequestMapping("/hw")
public class HelloWorldJSend {

    public static class Hello {
        final String hello;

        public Hello(String hello) {
            this.hello = hello;
        }
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello " + System.currentTimeMillis();
    }


    @RequestMapping("/helloj")
    public JSendResponse<String> helloJSend() {

        return JSendResponseBuilder.jSendResponseBuilder("hello " + System.currentTimeMillis()).build();
    }


    @RequestMapping("/helloj2")
    public JSendResponse<Hello> helloJSend2() {

        return JSendResponseBuilder.jSendResponseBuilder(new Hello("hello " + System.currentTimeMillis())).build();
    }


    @RequestMapping("/helloj3")
    public JSendResponse<List<String>> helloJSend3() {

        return JSendResponseBuilder.jSendResponseBuilder(Lists.list("hello " + System.currentTimeMillis())).build();
    }


    @RequestMapping("/helloj4")
    public JSendResponse<List<Hello>> helloJSend4() {

        return JSendResponseBuilder.jSendResponseBuilder(Lists.list(new Hello("hello " + System.currentTimeMillis()))).build();
    }


    @RequestMapping("/helloj5")
    public void helloJSend5(Callback<JSendResponse<List<Hello>>> callback) {

        callback.returnThis(JSendResponseBuilder.jSendResponseBuilder(Lists.list(new Hello("hello " + System.currentTimeMillis()))).build());
    }

    @RequestMapping("/helloj6")
    public void helloJSend6(Callback<JSendResponse<List<String>>> callback) {
        callback.returnThis(JSendResponseBuilder.jSendResponseBuilder(Lists.list(
                "hello " + System.currentTimeMillis())).build());
    }

    public static void main(final String... args) {
        final ManagedServiceBuilder managedServiceBuilder =
                ManagedServiceBuilder.managedServiceBuilder().setRootURI("/root");

        /* Start the service. */
        managedServiceBuilder.addEndpointService(new HelloWorldJSend())
                .getEndpointServerBuilder()
                .build().startServer();

        /* Start the admin builder which exposes health end-points and meta data. */
        managedServiceBuilder.getAdminBuilder().build().startServer();

        System.out.println("Servers started");

        managedServiceBuilder.getSystemManager().waitForShutdown();


    }


}
