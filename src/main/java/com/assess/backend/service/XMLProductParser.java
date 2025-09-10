package com.assess.backend.service;

import com.assess.backend.model.Product;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class XMLProductParser {

//    public List<Product> parseProductsFromXML(String xmlSource) {
//        return parseProductsFromXML(xmlSource, Integer.MAX_VALUE); // No limit by default
//    }

    public List<Product> parseProductsFromXML(String xmlSource, int limit, int offset) {
        List<Product> products = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document;

            // Check if xmlSource is a URL or a file path
            if (xmlSource.startsWith("http://") || xmlSource.startsWith("https://")) {
                System.out.println("Fetching XML from URL: " + xmlSource);
                document = parseFromURL(builder, xmlSource);
            } else {
                System.out.println("Reading XML from file: " + xmlSource);
                document = builder.parse(new File(xmlSource));
            }

            // Get all item elements
            NodeList itemNodes = document.getElementsByTagName("item");

            System.out.println("Found " + itemNodes.getLength() + " total products in XML");
            System.out.println("Offset: " + offset + " (skipping first " + offset + " products)");
            System.out.println("Limit: " + limit + " products to process");
            System.out.println("Range: Products " + (offset + 1) + " to " + (offset + limit));

            int processedCount = 0;
            int currentIndex = 0;

            for (int i = 0; i < itemNodes.getLength() && processedCount < limit; i++) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {

                    // Skip products until we reach the offset
                    if (currentIndex < offset) {
                        currentIndex++;
                        continue;
                    }

                    Element itemElement = (Element) itemNode;
                    Product product = parseProductFromItem(itemElement);
                    if (product != null) {
                        products.add(product);
                        processedCount++;
                        System.out.println("Processed product " + (offset + processedCount) + ": " + product.getTitle());
                    }
                    currentIndex++;
                }
            }

            System.out.println("Successfully processed " + products.size() + " products (offset: " + offset + ", limit: " + limit + ")");

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing XML source: " + e.getMessage(), e);
        }

        return products;
    }


    private Document parseFromURL(DocumentBuilder builder, String urlString) throws IOException, SAXException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set headers to mimic a browser request
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setRequestProperty("Accept", "application/xml, text/xml, */*");
        connection.setConnectTimeout(30000); // 30 seconds
        connection.setReadTimeout(60000); // 60 seconds

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    return builder.parse(inputStream);
                }
            } else {
                throw new IOException("HTTP Error: " + responseCode + " - " + connection.getResponseMessage());
            }
        } finally {
            connection.disconnect();
        }
    }

    private Product parseProductFromItem(Element itemElement) {
        Product product = new Product();

        try {
            // Extract id from g:id (text content)
            String id = getTextContent(itemElement, "g:id");
            product.setId(id);

            // Extract title from g:title (CDATA)
            String title = getCDataContent(itemElement, "g:title");
            product.setTitle(title);

            // Extract description from g:description (CDATA)
            String description = getCDataContent(itemElement, "g:description");
            product.setDescription(description);

            // Extract URL from g:link (CDATA)
            String url = getCDataContent(itemElement, "g:link");
            product.setUrl(url);

            // Extract image URL from g:image_link (CDATA)
            String imageUrl = getCDataContent(itemElement, "g:image_link");
            product.setImageUrl(imageUrl);

            // Extract price from g:price (text content) and convert to BigDecimal
            String priceText = getTextContent(itemElement, "g:price");
            if (priceText != null && !priceText.trim().isEmpty()) {
                // Remove currency code and convert to BigDecimal
                String cleanPrice = priceText.replaceAll("[^0-9.]", "");
                if (!cleanPrice.isEmpty()) {
                    product.setPrice(new BigDecimal(cleanPrice));
                }
            }

            // Extract occasions from multiple occasion elements (CDATA) into categories list
            List<String> occasions = getAllCDataContent(itemElement, "occasion");
            product.setCategories(occasions);

            // Extract additional fields that are available in your XML
            String brand = getCDataContent(itemElement, "g:brand");
            product.setBrand(brand);

            String gender = getCDataContent(itemElement, "g:gender");
            product.setGender(gender);

            String type = getCDataContent(itemElement, "g:product_type");
            product.setType(type);

            return product;

        } catch (Exception e) {
            System.err.println("Error parsing product item: " + e.getMessage());
            return null;
        }
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent().trim();
        }
        return null;
    }

    private String getCDataContent(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    if (child.getNodeType() == Node.CDATA_SECTION_NODE ||
                            child.getNodeType() == Node.TEXT_NODE) {
                        String content = child.getNodeValue();
                        if (content != null && !content.trim().isEmpty()) {
                            return content.trim();
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<String> getAllCDataContent(Element parent, String tagName) {
        List<String> values = new ArrayList<>();
        NodeList nodeList = parent.getElementsByTagName(tagName);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    if (child.getNodeType() == Node.CDATA_SECTION_NODE ||
                            child.getNodeType() == Node.TEXT_NODE) {
                        String content = child.getNodeValue();
                        if (content != null && !content.trim().isEmpty()) {
                            values.add(content.trim());
                        }
                    }
                }
            }
        }

        return values;
    }
}