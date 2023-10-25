package com.netflop.global.resource;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.cognito.*;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.TableV2;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.EventType;
import software.amazon.awscdk.services.s3.notifications.SnsDestination;
import software.amazon.awscdk.services.sns.Topic;
import software.constructs.Construct;

public class NetflopGlobalResourceStack extends Stack {

    public NetflopGlobalResourceStack(final Construct scope, final String id) {
        super(scope, id, null);
    }

    public NetflopGlobalResourceStack(final Construct scope, String id, final StackProps props){

        super(scope, id, props);

        // TO-DO
        // Add AWS Global Resource Here

        // Get environment
        String env = (String)this.getNode().tryGetContext("env");
        if(env == null || env.isEmpty()) {
            env = "dev";
        }

        // SNS for notification
        Topic topic = Topic.Builder.create(this, "netflop-movie-sns")
                .topicName("netflop-movie-sns-" + env)
                .build();

        // Bucket to store movie resource
        Bucket bucket = Bucket.Builder.create(this, "netflop-movie-storage")
                .versioned(true)
                .encryption(BucketEncryption.KMS_MANAGED)
                .bucketName("netflop-movie-storage-" + env)
                .build();

        bucket.addEventNotification(EventType.OBJECT_CREATED, new SnsDestination(topic));

        // DynamoDB
        // Table user
        TableV2 user = TableV2.Builder.create(this, "netflop-user-dynamodb")
                .tableName("netflop-user-" + env)
                .partitionKey(Attribute.builder().name("user-id").type(AttributeType.STRING).build())
                .build();

        // Table movie
        TableV2 movie = TableV2.Builder.create(this, "netflop-movie-dynamodb")
                .tableName("netflop-movie-" + env)
                .partitionKey(Attribute.builder().name("movie-id").type(AttributeType.STRING).build())
                .build();

        // User pool
        UserPool pool = UserPool.Builder.create(this, "netflop-user-pool")
                .userPoolName("netflop-user-pool-" + env)
                .signInAliases(
                        SignInAliases.builder()
                                .email(true)
                                .username(true)
                                .phone(true)
                                .build())
                .autoVerify(
                        AutoVerifiedAttrs.builder()
                                .email(true)
                                .phone(true)
                                .build())
                .passwordPolicy(
                        PasswordPolicy.builder()
                                .minLength(8)
                                .requireDigits(true)
                                .requireLowercase(true)
                                .requireSymbols(true)
                                .requireUppercase(true)
                                .tempPasswordValidity(Duration.days(7))
                                .build())
                .accountRecovery(AccountRecovery.EMAIL_ONLY)
                .selfSignUpEnabled(true)
                .userVerification(
                        UserVerificationConfig.builder()
                                .emailSubject("[Netflop] Please confirm your email address")
                                .emailBody("Thanks for signing up to Netflop app! Your verification code is {####}")
                                .emailStyle(VerificationEmailStyle.CODE)
                                .build())
                .userInvitation(
                        UserInvitationConfig.builder()
                                .emailSubject("[Netflop] Your temporary password")
                                .emailBody("Thanks for signing up to Netflop app! Your username is {username} and temporary password is {####}")
                                .build())
                .standardAttributes(
                        StandardAttributes.builder()
                                .fullname(StandardAttribute.builder().required(true).mutable(false).build())
                                .build())
                .keepOriginal(
                        KeepOriginalAttrs.builder()
                                .email(true)
                                .phone(true)
                                .build())
                .email(UserPoolEmail.withCognito())
                .build();

        // User pool client
        UserPoolClient poolClient = pool.addClient("app-client", UserPoolClientOptions.builder()
                        .userPoolClientName("app-client-" + env)
                        .generateSecret(false)
                        .authFlows(AuthFlow.builder()
                                .userPassword(true)
                                .build())
                        .authSessionValidity(Duration.minutes(3))
                        .refreshTokenValidity(Duration.days(30))
                        .accessTokenValidity(Duration.minutes(60))
                        .idTokenValidity(Duration.minutes(60))
                        .enableTokenRevocation(true)
                        .preventUserExistenceErrors(true)
                .build());

        // Add user pool group
        CfnUserPoolGroup adminPoolGroup = CfnUserPoolGroup.Builder.create(this, "admin-pool-group")
                .userPoolId(pool.getUserPoolId())
                .groupName("Admin")
                .description("The admin group of Netflop pool")
                .build();

        CfnUserPoolGroup userPoolGroup = CfnUserPoolGroup.Builder.create(this, "user-pool-group")
                .userPoolId(pool.getUserPoolId())
                .groupName("User")
                .description("The user group of Netflop pool")
                .build();
    }
}
