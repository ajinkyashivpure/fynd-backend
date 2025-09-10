//package com.assess.backend.service;
//
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.datavec.image.loader.NativeImageLoader;
//import org.deeplearning4j.nn.graph.ComputationGraph;
//import org.deeplearning4j.nn.transferlearning.TransferLearningHelper;
//import org.deeplearning4j.zoo.ZooModel;
//import org.deeplearning4j.zoo.model.VGG16;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.dataset.DataSet;
//import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
//import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
//import org.nd4j.linalg.factory.Nd4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Service
//@Slf4j
//public class ImageEmbeddingService {
//    private ComputationGraph pretrainedModel;
//    private TransferLearningHelper transferLearningHelper;
//
//    @PostConstruct
//    public void init() {
//        try {
//            // Load the complete pre-trained VGG16 model
//            ZooModel zooModel = VGG16.builder().build();
//            ComputationGraph fullModel = (ComputationGraph) zooModel.initPretrained();
//
//            log.info("Model has {} layers", fullModel.getLayers().length);
//            for (org.deeplearning4j.nn.api.Layer layer : fullModel.getLayers()) {
//                log.info("Layer: {}", layer.conf().getLayer().getLayerName());
//            }
//
//            // Instead of trying to rebuild the network layer by layer, we'll use the feature extraction approach
//            // This approach is safer and uses DL4J's built-in functionality for feature extraction
//
//            // The VGG16 model typically has "fc1" as the first dense layer after flattening
//            // We'll use DL4J's ability to extract features from a specific layer
//
//            TransferLearningHelper transferLearningHelper = new TransferLearningHelper(
//                    fullModel, "fc1");  // Extract features from the "fc1" layer
//
//            // Store this helper for later use when generating embeddings
//            this.transferLearningHelper = transferLearningHelper;
//
//            // Verify the output size with a dummy input
//            INDArray dummyInput = Nd4j.zeros(1, 3, 224, 224);
//            DataSet dummyDataSet = new DataSet(dummyInput, Nd4j.zeros(1, 1000)); // Output doesn't matter for feature extraction
//            INDArray features = transferLearningHelper.featurize(dummyDataSet).getFeatures();
//
//            log.info("Output feature shape: {}", Arrays.toString(features.shape()));
//            if (features.length() != 4096) {
//                log.warn("Expected output length 4096, but got {}", features.length());
//            }
//
//            log.info("Image feature extraction model loaded successfully");
//        } catch (Exception e) {
//            log.error("Failed to initialize image embedding model", e);
//            throw new RuntimeException("Image embedding model initialization failed", e);
//        }
//    }
//
//    public List<Double> generateImageEmbeddings(String imageUrl) {
//        try {
//            // Download image from URL
//            URL url = new URL(imageUrl);
//            BufferedImage image = ImageIO.read(url);
//
//            if (image == null) {
//                throw new RuntimeException("Failed to load image from URL: " + imageUrl);
//            }
//
//            BufferedImage resizedImage = new BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB);
//            Graphics2D g = resizedImage.createGraphics();
//            g.drawImage(image, 0, 0, 224, 224, null);
//            g.dispose();
//
//            // Load the image
//            NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
//            INDArray imageArray = loader.asMatrix(resizedImage);
//
//            // Apply VGG16 preprocessing
//            DataNormalization scaler = new VGG16ImagePreProcessor();
//            scaler.transform(imageArray);
//
//            // Create a dummy dataset (labels don't matter for feature extraction)
//            DataSet dataSet = new DataSet(imageArray, Nd4j.zeros(1, 1000));
//
//            // Extract features using the transfer learning helper
//            INDArray features = transferLearningHelper.featurize(dataSet).getFeatures();
//
//            // Convert to List<Double> for MongoDB storage
//            List<Double> embeddings = new ArrayList<>();
//            for (int i = 0; i < features.length(); i++) {
//                embeddings.add(features.getDouble(i));
//            }
//
//            return embeddings;
//        } catch (Exception e) {
//            log.error("Failed to generate image embeddings", e);
//            throw new RuntimeException("Image embedding generation failed", e);
//        }
//    }
//
//    public List<Double> generateImageEmbeddingsFromFile(MultipartFile file) {
//        try {
//            // Read image from MultipartFile
//            BufferedImage image = ImageIO.read(file.getInputStream());
//
//            if (image == null) {
//                throw new RuntimeException("Failed to load image from uploaded file");
//            }
//
//            // Resize image to match VGG16 input size (224x224)
//            BufferedImage resizedImage = new BufferedImage(224, 224, BufferedImage.TYPE_INT_RGB);
//            Graphics2D g = resizedImage.createGraphics();
//            g.drawImage(image, 0, 0, 224, 224, null);
//            g.dispose();
//
//            // Convert image to INDArray
//            NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
//            INDArray imageArray = loader.asMatrix(resizedImage);
//
//            // Preprocess the image
//            DataNormalization scaler = new VGG16ImagePreProcessor();
//            scaler.transform(imageArray);
//
//            // Create a dummy dataset (labels don't matter for feature extraction)
//            DataSet dataSet = new DataSet(imageArray, Nd4j.zeros(1, 1000));
//
//            // Extract features using the transfer learning helper
//            INDArray features = transferLearningHelper.featurize(dataSet).getFeatures();
//
//            // Convert to List<Double>
//            List<Double> embeddings = new ArrayList<>();
//            for (int i = 0; i < features.length(); i++) {
//                embeddings.add(features.getDouble(i));
//            }
//
//            return embeddings;
//        } catch (Exception e) {
//            log.error("Failed to generate image embeddings from file", e);
//            throw new RuntimeException("Image embedding generation failed", e);
//        }
//    }
//}