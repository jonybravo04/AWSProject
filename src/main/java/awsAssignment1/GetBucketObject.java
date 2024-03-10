package awsAssignment1;

import java.net.URL;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class GetBucketObject {

	private AmazonS3 s3Client;						   //SSM client connection variable initialize
    
    // fetching Access Key and Secret Key from environment variable
    public static final String AWS_Access_Key = System.getenv("AWS_ACCESS_KEY");
	public static final String AWS_Secret_Key = System.getenv("AWS_SECRET_KEY");
	
	public GetBucketObject() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(AWS_Access_Key, AWS_Secret_Key);
        
        // S3 Client Connection
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion("ap-south-1")
                .build();
    }
	
	public static void main(String[] args) {
		
		FetchParam fp = new FetchParam();
		String parameterValue = fp.getParameterFromParameterStore("/ASSIGNMENT/S3-Bucket", true);
        System.out.println("Parameter Value: " + parameterValue);
        
        //retrieving Bucket Name via the parameter value fetched
        GetBucketObject bo = new GetBucketObject();
        boolean found = false;
        for (Bucket bucket : bo.s3Client.listBuckets()) {
        	
                if (parameterValue.equals(bucket.getName())) {
                    System.out.println("Match Found: " + bucket.getName());
                    found = true;

                    // retrieving the objects from the bucket name matched with parameter value
                    ListObjectsV2Result result = bo.s3Client.listObjectsV2(bucket.getName());
                    List<S3ObjectSummary> objects = result.getObjectSummaries();
                    for (S3ObjectSummary os : objects) {
                        String path = "* " + os.getKey();
                        System.out.println(path);
                        String completepath = bo.s3Client.getUrl(bucket.getName(), os.getKey()).toString();
                        System.out.println(completepath);

                        // Sending email with the URL
                        EmailObject eo = new EmailObject();
                        eo.mail(completepath);
                    }
                    // Break out of the loop since we found a match
                    break;
                }
            }

            if (!found) {
                System.out.println("No match found for parameter value: " + parameterValue);
            }
        }
    }