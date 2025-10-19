package com.ecom.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

@Configuration
public class IndexConfig {

	@Value("${bucketName}")
	private String bucketName;

	@Bean
	public IndexSearcher getIndexSearcher() throws Exception {
		// get s3 client
		S3Client s3Client = S3Client.create();
		String sourceFilePath = "items/index/index.zip";
		String zipFileName = "index.zip";

		// download files from S3
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(sourceFilePath).build();
		ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
		FileOutputStream outputStream = new FileOutputStream(zipFileName);

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = s3Object.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();

		// unzip files
		extractZip(zipFileName, "index");

		FSDirectory indexDirectory = FSDirectory.open(Paths.get("index"));
		IndexReader reader = DirectoryReader.open(indexDirectory);
		IndexSearcher searcher = new IndexSearcher(reader);

		return searcher;
	}

	public void extractZip(String zipFile, String outputDirPath) throws Exception {
		File outputDir = new File(outputDirPath);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		try (ZipFile zip = new ZipFile(new File(zipFile))) {
			Enumeration<ZipArchiveEntry> entries = zip.getEntries();

			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				File outFile = new File(outputDir, entry.getName());

				if (entry.isDirectory()) {
					outFile.mkdirs();
					continue;
				}

				File parent = outFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}

				try (InputStream is = zip.getInputStream(entry); FileOutputStream fos = new FileOutputStream(outFile)) {

					byte[] buffer = new byte[1024];
					int len;
					while ((len = is.read(buffer)) != -1) {
						fos.write(buffer, 0, len);
					}
				}

				System.out.println("Extracted: " + outFile.getAbsolutePath());
			}
		}
	}

}
