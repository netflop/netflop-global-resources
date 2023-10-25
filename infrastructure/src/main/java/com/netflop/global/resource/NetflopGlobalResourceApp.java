package com.netflop.global.resource;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class NetflopGlobalResourceApp {

    public static void main (final String[] args) {

        App app = new App();

        // Get environment
        String env = (String)app.getNode().tryGetContext("env");
        if(env == null || env.isEmpty()) {
            env = "dev";
        }

        new NetflopGlobalResourceStack(app, "netflop-global-resource-" + env, StackProps.builder()
                // If you don't specify 'env', this stack will be environment-agnostic.
                // Account/Region-dependent features and context lookups will not work,
                // but a single synthesized template can be deployed anywhere.

                // Uncomment the next block to specialize this stack for the AWS Account
                // and Region that are implied by the current CLI configuration.
                /*
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                */

                // Uncomment the next block if you know exactly what Account and Region you
                // want to deploy the stack to.
                /*
                .env(Environment.builder()
                        .account("123456789012")
                        .region("us-east-1")
                        .build())
                */
                .build());

        app.synth();
    }
}
