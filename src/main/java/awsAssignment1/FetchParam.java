package awsAssignment1;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;

public class FetchParam {
	private AWSSimpleSystemsManagement ssmClient;      //S3 client connection variable initialize
    
    // fetching Access Key and Secret Key from environment variable
    public static final String AWS_Access_Key = System.getenv("AWS_ACCESS_KEY");
	public static final String AWS_Secret_Key = System.getenv("AWS_SECRET_KEY");
	
	// Client connection
    public FetchParam() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(AWS_Access_Key, AWS_Secret_Key);
        
        // SSS Client Connection
        this.ssmClient = AWSSimpleSystemsManagementClientBuilder.standard()
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

}
