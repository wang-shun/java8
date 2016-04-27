package io.terminus.doctor.workflow.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Desc: xml帮助类
 *      1. xml文档转Document对象
 *      2. Document对象转xml文档
 *      3. 获取指定Node对象
 *      4. 获取指定NodeList对象
 *      5. 获取指定Node的属性值
 *      6. 获取指定Node的唯一子节点
 *      7. 获取指定Node的所有子节点
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/4/26
 */
public class XmlHelper {

    /**
     * xml文档转为Document对象
     * @param inputStream   输入流
     * @return  Document对象
     * @throws Exception
     */
    public static Document toDocument(InputStream inputStream) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
    }

    /**
     * Document对象转xml文档
     * @param document
     * @return
     * @throws Exception
     */
    public static String toXml(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty("indent","no");
        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(sw));
        return sw.toString();
    }

    /**
     * 根据表达式获取指定节点
     * @param expression    表达式
     * @param document      Document对象
     * @return  Node对象
     * @throws Exception
     */
    public static Node getNode(String expression, Document document) throws Exception {
        XPath path = XPathFactory.newInstance().newXPath();
        return (Node) path.evaluate(expression, document, XPathConstants.NODE);
    }

    /**
     * 根据表达式获取指定节点列表
     * @param expression    表达式
     * @param document      Document对象
     * @return
     * @throws Exception
     */
    public static NodeList getNodeList(String expression, Document document) throws Exception {
        XPath path = XPathFactory.newInstance().newXPath();
        return (NodeList) path.evaluate(expression, document, XPathConstants.NODESET);
    }

    /**
     * 获取指定Node中的属性值
     * @param node      指定Node
     * @param propName  属性名称
     * @return
     */
    public static String getAttrValue(Node node, String propName) {
        if(node != null) {
            Node attr = node.getAttributes().getNamedItem(propName);
            if(attr == null) {
                return null;
            }
            return attr.getNodeValue();
        }
        return null;
    }

    /**
     * 获取指定Node中的属性值(double类型)
     * @param node      指定节点
     * @param propName  属性名称
     * @return
     */
    public static Double getAttrDoubleValue(Node node, String propName) {
        if(node != null) {
            Node attr = node.getAttributes().getNamedItem(propName);
            if(attr == null) {
                return null;
            }
            String value = attr.getNodeValue();
            if(StringUtils.isNoneBlank(value)) {
                return Double.parseDouble(value.trim());
            }
        }
        return null;
    }

    /**
     * 根据节点名称获取指定节点的唯一子节点
     * @param node      指定节点
     * @param nodeName  子节点名称
     * @return
     */
    public static Node getChildrenSingleNode(Node node, String nodeName) {
        List<Node> nodes = getChildrenNodes(node, nodeName);
        if(nodes != null && nodes.size() > 1) {
            AssertHelper.throwException("当前节点存在多个子节点, 当前节点名称为: {}, 节点名称为: {}", node.getNodeName(), nodeName);
        }
        if(nodes != null && nodes.size() == 0) {
            AssertHelper.throwException("当前节点不存在子节点, 当前节点名称为: {}, 节点名称为: {}", node.getNodeName(), nodeName);
        }
        return nodes.get(0);
    }

    /**
     * 根据节点名称获取指定节点的所有节点
     * @param node      指定节点
     * @param nodeName  子节点名称
     * @return
     */
    public static List<Node> getChildrenNodes(Node node, String nodeName) {
        List<Node> list = Lists.newArrayList();
        NodeList chilNodes = node.getChildNodes();
        for (int i = 0; chilNodes != null && i < chilNodes.getLength(); i++) {
            Node chilNode = chilNodes.item(i);
            if(StringUtils.isNoneBlank(nodeName) && nodeName.equals(chilNode.getNodeName())) {
                list.add(chilNode);
            }
        }
        return list;
    }



    public static void main(String[] args) throws Exception {

        String aa = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!-- 流程开始 -->\n" +
                "<workflow key=\"simpleFlow\" name=\"简单流程测试\">\n" +
                "    <!-- 开始节点 -->\n" +
                "    <start name=\"开始节点\" pointx=\"50\" pointy=\"100\">\n" +
                "        <transition name=\"开始连线\" target=\"任务节点1\" />\n" +
                "    </start>\n" +
                "    <!-- 任务节点 -->\n" +
                "    <task name=\"任务节点1\" assignee=\"\" pointx=\"250\" pointy=\"100\">\n" +
                "        <transition name=\"任务连线\" target=\"结束节点\" />\n" +
                "    </task>\n" +
                "\n" +
                "    <!-- 结束节点 -->\n" +
                "    <end name=\"结束节点\" pointx=\"450\" pointy=\"100\" />\n" +
                "</workflow>";

        InputStream inputStream = new ByteArrayInputStream(aa.getBytes());
        Document document = toDocument(inputStream);
        Node root = getNode("/workflow",document);
        System.out.println(root.getAttributes().getNamedItem("key").getNodeValue());
        Node start = getNode("workflow/start", document);
        // start 下的节点
        Node tran1 = getNode("/workflow/start/transition", document);
        System.out.println(tran1.getAttributes().getNamedItem("name").getNodeValue());

    }
}
