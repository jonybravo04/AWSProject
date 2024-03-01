package awsproject;

import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;

public class awsAssignment {
    private AWSSimpleSystemsManagement ssmClient;      //S3 client connection variable initialize
    private AmazonS3 s3Client;						   //SSM client connection variable initialize
    
    // fetching Access Key and Secret Key from environment variable
    public static final String AWS_Access_Key = System.getenv("AWS_ACCESS_KEY");
	public static final String AWS_Secret_Key = System.getenv("AWS_SECRET_KEY");
	
	// Client connection
    public awsAssignment() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(AWS_Access_Key, AWS_Secret_Key);
        
        // SSS Client Connection
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion("ap-south-1")
                .build();
        
        // S3 Client Connection
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion("ap-south-1")
                .build();
    }

    // Methods to get Parameter from SSM Parameter Store
    public String getParameterFromParameterStore(String parameterName, boolean withDecryption) {
        GetParameterRequest getParameterRequest = new GetParameterRequest()
                .withName(parameterName)
                .withWithDecryption(withDecryption);
        GetParameterResult result = ssmClient.getParameter(getParameterRequest);
        return result.getParameter().getValue();
    }

    /* The Bucket name will be called in Main method by getting the parameter value from the store
     * and matching it with S3 Bucket Name
    */
    
    //Main method
    public static void main(String[] args) {
    	awsAssignment client = new awsAssignment();
        
        //fetching parameter value by passing Parameter Name
        String parameterValue = client.getParameterFromParameterStore("/ASSIGNMENT/S3-Bucket", true);
        System.out.println("Parameter Value: " + parameterValue);

        //retrieving Bucket Name via the parameter value fetched
        for (Bucket bucket : client.s3Client.listBuckets()) {
        	
                if (parameterValue.equals(bucket.getName())) {
                    System.out.println("Match Found: " + bucket.getName());
                    
                    // retrieving the objects from the bucket name matched with parameter value
                    ListObjectsV2Result result = client.s3Client.listObjectsV2(bucket.getName());
                    List<S3ObjectSummary> objects = result.getObjectSummaries();
                    for (S3ObjectSummary os : objects) {
                    System.out.println("* " + os.getKey());
                  }
                }else {
                    System.out.println("No Match: " + bucket.getName());
                }
        }
        
   // AWS SES *****************************************************
        String FROM = "jonybravo1604@gmail.com";		//Verified Email IDs
        String TO = "jonybravo1604@gmail.com";
        String SUBJECT = "Email using Amazon SES";
        String TEXTBODY = "This email was sent through Amazon SES "
        	      + "The value of Parameter used to fetch S3 Bucket is";
        
        //Mail being sent to the User returning the parameterValue used to fetch S3 Bucket
        try {
            AmazonSimpleEmailService sesClient = 
                AmazonSimpleEmailServiceClientBuilder.standard()
              
                 .withRegion("ap-south-1").build();
            SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(TO))
                .withMessage(new Message()
                .withBody(new Body()
                .withHtml(new Content()
                //.withCharset("UTF-8").withData(" The Value of Parameter is "+parameterValue))
                //.withText(new Content()
                .withCharset("UTF-8").withData(TEXTBODY + " : "+ parameterValue)))
                .withSubject(new Content()
                .withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);
               
            sesClient.sendEmail(request);
            System.out.println("Email sent to the User!");
          } catch (Exception ex) {
            System.out.println("Email was not sent. Error message: " + ex.getMessage());
          }
        
        
    }
} 
    
