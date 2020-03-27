# PDIPlugin-AWS-SNS
_Amazon Webservices Simple Notification Service Plugin for Pentaho Data Integration_

The AWS-SNS Plugin enables you to send Notifications from a PDI Transformation via AWS SNS 
to subscribed users.



## System Requirements

- Pentaho Data Integration 7.1 or above



## Installation

**Using Pentaho Marketplace (recommended)**

1. In the Pentaho Marketplace find the AWS SNS Notify Plugin and click Install
2. Restart Spoon

**Manual Install**

1. Create ${DI\_HOME}/plugins/steps/AWS-SNS-Notify directory (if not exists)
2. Create ${DI\_HOME}/plugins/steps/AWS-SNS-Notify/lib directory (if not exists)
3. Copy/Replace the AWS-SNS-Notify-Plugin jar into the ${DI\_HOME}/plugins/steps/AWS-SNS-Notify directory
4. Copy/Replace the lib/ folder into the ${DI\_HOME}/plugins/steps/AWS-SNS-Notify/lib directory
5. Restart Spoon



## Manual build

To build (with Apache Maven):

```shell
mvn package
```



## Documentation

For detailled information on how to configure and use the plugin please have a look at the [Documentation](https://freddyffm.github.io/PDIPlugin-AWS-SNS/).



## About Amazon SNS

[Amazon Simple Notification Service (Amazon SNS)][sns] is a fast, fully-managed,
push messaging service. Amazon SNS can deliver messages to email, mobile devices
(i.e., SMS; iOS, Android and FireOS push notifications), Amazon SQS queues,and
— of course — HTTP/HTTPS endpoints.

With Amazon SNS, you can setup topics to publish custom messages to subscribed
endpoints. However, SNS messages are used by many of the other AWS services to
communicate information asynchronously about your AWS resources. Some examples
include:

* Configuring Amazon Glacier to notify you when a retrieval job is complete.
* Configuring AWS CloudTrail to notify you when a new log file has been written.
* Configuring Amazon Elastic Transcoder to notify you when a transcoding job
  changes status (e.g., from "Progressing" to "Complete")

Though you can certainly subscribe your email address to receive SNS messages
from service events like these, your inbox would fill up rather quickly. There
is great power, however, in being able to subscribe an HTTP/HTTPS endpoint to
receive the messages. This allows you to program webhooks for your applications
to easily respond to various events.



## Author

- [Michael Fraedrich](https://github.com/FreddyFFM/)



[sns]: https://aws.amazon.com/sns/
[jackson]: https://github.com/FasterXML/jackson-databind/wiki
[aws-sdk]: https://aws.amazon.com/de/sdk-for-java/
[joda]: https://github.com/JodaOrg/joda-time/releases