# Installation

## System Requirements

- Pentaho Data Integration 7.0 or above
- Copy following jar files to ${DI\_HOME}/lib
    - [Jackson Databind][jackson] (Version 2.9.3 or higher - current version needs to be replaced)
    - [Amazon Java SDK for SNS][aws-sdk] (Version 1.11.269 or higher)
    - [Joda Time][joda] (Version 2.9.9 or higher - current version needs to be replaced)



## Installation

**Using Pentaho Marketplace (recommended)**

1. In the Pentaho Marketplace find the AWS SNS Notify Plugin and click Install
2. Restart Spoon

For further information please do also have a look at the [readme.md](https://github.com/FreddyFFM/PDIPlugin-AWS-SNS/blob/master/README.md)


[jackson]: https://github.com/FasterXML/jackson-databind/wiki
[aws-sdk]: https://aws.amazon.com/de/sdk-for-java/
[joda]: https://github.com/JodaOrg/joda-time/releases