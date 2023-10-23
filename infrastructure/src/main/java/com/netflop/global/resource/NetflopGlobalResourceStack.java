package com.netflop.global.resource;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableV2;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.BucketProps;
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
    }
}
