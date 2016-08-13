package edu.thesis.web.service.rest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.recognition.FaceRecognitionEngine;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.w3c.dom.svg.GetSVGDocument;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

@Path("/rest")
public class ImageServerImpl implements ImageServer {

	private EntityManagerFactory emf;
	private EntityManager em;
	private String PERSISTENCE_UNIT_NAME = "hyrax-server";
	private static final int BUFFER_SIZE = 4096;
	private static final String HOME_PATH = System.getProperty("user.home");

	@Context
	private ServletContext context;

	public ImageServerImpl(){
		initEntityManager();
	}

	private void initEntityManager() {
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		em = emf.createEntityManager();
	}

	@GET
	@Path("/images/{imageId}")
	@Produces("image/jpg")
	public Response downloadImage(@PathParam("imageId") String imageId) {
		ImageEntity img = (ImageEntity) em.createQuery("select i from ImageEntity i where i.id = :imageId ")
				.setParameter("imageId", Integer.parseInt(imageId))
				.getSingleResult();
		File f = new File(img.getPath());
		ResponseBuilder response = Response.ok((Object) f);
		response.header("Content-Disposition",
				"attachment; filename=" + f.getName());
		return response.build();
	}
	
	/*@POST
	@Path("/register")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response registerFace(FormDataMultiPart formParams)
	{
		Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
		for (List<FormDataBodyPart> fields : fieldsByName.values())
		{
			for (FormDataBodyPart field : fields)
			{
				if (field.getName().equals("uploaded_file")){
					InputStream is = field.getEntityAs(InputStream.class);
					String fileName = field.getContentDisposition().getFileName();
					String userName = fileName.split("\\.")[0];
					File userFolder = new File(HOME_PATH + File.separator + "HyraxUsers" + File.separator + userName);
					if (!userFolder.exists()){
						userFolder.mkdir();
					}
					File zip = new File( userFolder.getAbsolutePath() + File.separator + fileName);
					System.out.println(zip.getAbsolutePath());
					writeToFile(is, zip.getAbsolutePath());
					String unzipedPath = zip.getParentFile().getAbsolutePath() + File.separator + "unziped";
					try {
						unzip (zip.getAbsolutePath(), unzipedPath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					System.out.println("File unziped");

					File [] unzipedFolder = new File(unzipedPath).listFiles();
					engine = (FaceRecognitionEngine<KEDetectedFace, String>) context.getAttribute("engine");
					for (File f : unzipedFolder){
						try {
							engine.train(userName.toLowerCase(), ImageUtilities.readF(f));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					em.getTransaction().begin();
					User u = new User(userName.toLowerCase());
					em.persist(u);
					em.getTransaction().commit();

					System.out.println("User saved");

					String output = "Face processed sucessfully";

					return Response.status(200).entity(output).build();

				} 
			}

		}

		String output = "No images received";

		return Response.status(422).entity(output).build();

	}

	public void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		ZipEntry entry = zipIn.getNextEntry();
		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = zipIn.read(bytesIn)) != -1) {
			bos.write(bytesIn, 0, read);
		}
		bos.close();
	}*/

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadImage(FormDataMultiPart formParams)
	{
		Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();
		ImageEntity obj = new ImageEntity();
		File imageFile = null;
		for (List<FormDataBodyPart> fields : fieldsByName.values())
		{
			for (FormDataBodyPart field : fields)
			{
				 if (field.getName().equals("image")) {
					InputStream is = field.getEntityAs(InputStream.class);
					String fileName = field.getContentDisposition().getFileName();

					File imgDir = new File (HOME_PATH + File.separator + "HyraxImages");
					if (!imgDir.exists()){
						imgDir.mkdir();
					}

					String imageLocation =  imgDir.getAbsolutePath() + File.separator + fileName ;

					imageFile = new File(imageLocation);

					if (!imageFile.getParentFile().exists()){
						imageFile.getParentFile().mkdirs();
					}

					// save it
					writeToFile(is, imageLocation);		
				} 
			}
		}
		System.out.println(imageFile.getAbsolutePath());
		

		obj.setPath(imageFile.getAbsolutePath());
		
		
		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();

		String output = "File uploaded to successfuly";

		return Response.status(200).entity(String.valueOf(obj.getId())).build();

	}

	
	
	/*@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadImage(FormDataMultiPart formParams)
	{
	    Map<String, List<FormDataBodyPart>> fieldsByName = formParams.getFields();

	    File imgDir = new File ("HyraxImages/");
		if (!imgDir.exists()){
			imgDir.mkdir();
		}

		String folderName = "";
		File imageFile = null;
		ImageEntity obj = null;
		Set<Face> faces = new HashSet<Face>(); 

	    for (List<FormDataBodyPart> fields : fieldsByName.values())
	    {
	        for (FormDataBodyPart field : fields)
	        {
	        	System.out.println(field.getName());

	        	if (field.getName().equals("details")){
	        		ObjectMapper mapper = new ObjectMapper();
	        		String details = field.getEntityAs(String.class);

	        		try {
	        			obj = mapper.readValue(details, ImageEntity.class);
	        		} catch (JsonParseException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();	        		} catch (JsonMappingException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		} catch (IOException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}

	        	} else if (field.getName().equals("image")) {
	        		InputStream is = field.getEntityAs(InputStream.class);
	        		String fileName = field.getContentDisposition().getFileName();
	        		if (folderName == ""){
	        			folderName = fileName.split("\\.")[0];
	        		}

		            String imageLocation =  imgDir.getAbsolutePath() + "/" + folderName + "/" + fileName ;

		            imageFile = new File(imageLocation);

		            if (!imageFile.getParentFile().exists()){
		            	imageFile.getParentFile().mkdirs();
		            }

		            // save it
		    		writeToFile(is, imageLocation);	

	        	} else if (field.getName().equals("face")){
	        		InputStream is = field.getEntityAs(InputStream.class);
	        		String [] info = field.getContentDisposition().getFileName().split("_");
	        		if (folderName == ""){
	        			folderName = info[0];
	        		}

		            String faceName = info[1];


	        		File faceFile = new File (imgDir.getAbsolutePath() + "/" + folderName + "/faces/" + faceName);

	                faceFile.getParentFile().mkdirs();

		            // save it
		    		writeToFile(is, faceFile.getAbsolutePath());

		    		Face face = new Face();
		    		face.setPath(faceFile.getAbsolutePath());
		    		faces.add(face);
	        	}

	        }
	    }

	    for(Face f : faces){
    		f.setI(obj);
    	}

    	obj.setFaces(faces);
    	obj.setPath(imageFile.getAbsolutePath());

    	em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();

	    String output = "File uploaded to successfuly";

		return Response.status(200).entity(output).build();
	}*/

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) {

		try {

			int read = 0;
			byte[] bytes = new byte[1024];

			File img = new File(uploadedFileLocation);
			OutputStream out = new FileOutputStream(img);
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/*private List<ImageEntity> recognizeInFaces(FaceRecognizer eigenFace, List<ImageEntity> images, Size size){
		List<ImageEntity> detectedImages = new ArrayList<ImageEntity>();
		double[] prediction = new double[1];
		int[] predictionImageLabel = new int[1]; 
		System.out.println("Number of images: " + images.size());
		for (ImageEntity i : images){
			for (Face f : i.getFaces()){
				System.out.println("Number of faces: " + i.getFaces().size());
				Mat face = processFace(f,size);
				eigenFace.predict(face, predictionImageLabel, prediction);
				if (prediction[0] < 5000000){
					System.out.println("HELLOOOOO - " + prediction[0]);
					detectedImages.add(i);
					break;
				}
			}

		}
		return detectedImages;
	}

	private Mat processFace(Face f, Size size) {
		Mat image = Imgcodecs.imread(f.getPath(), org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		Imgproc.resize(image, image, size);
		return image;
	}*/
}
