package com.lxz.examclustering;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DocToolTest {
	/**
	 * 返回余弦距离下与文档距离最近的中心点类标号
	 * @param doc 文档
	 * @param centers 所有中心点
	 * @param dictSize 词典大小
	 * @return 文档所属类标号
	 */
	public static int returnNearestCentNum(Map<String, Double> doc,
			Map<Integer, Map<String, Double>> centers) {
		//最近中心点
		int nearestCendroid = 0;
		//最小距离
		double nearestDistance = 0;
		//文档向量长度
		double docLength = 0;
		//中心点向量长度
		double centLength = 0;
		//文档向量与中心点向量内积
		double innerProduct = 0;
		
		//计算文档向量长度
		Iterator<Entry<String, Double>> docIter = doc.entrySet().iterator();
		while (docIter.hasNext()) {
			Map.Entry<String, Double> entry = (Map.Entry<String, Double>) docIter
					.next();
			docLength += Math.pow(entry.getValue(), 2.0);
		}
		docLength = Math.sqrt(docLength);//文档向量长度
		
		//计算文档与所有中心点的余弦距离
		Iterator<Entry<Integer, Map<String, Double>>> allCendroids = centers
				.entrySet().iterator();//取出每个中心点
		while (allCendroids.hasNext()) {
			Map.Entry<Integer, Map<String, Double>> entry = (Entry<Integer, Map<String, Double>>) allCendroids
					.next();
			Set docSet = doc.keySet();//取得所有doc值，依次判断
			for (Iterator iter = docSet.iterator(); iter.hasNext();) {
				if(entry.getValue().containsKey(iter.next())){//判断中心点和文档有没有共同的词
					innerProduct += entry.getValue().get(iter.next()) * doc.get(iter.next());//计算文档向量和中心点向量的内积
				}
//				String key = (String) iter.next();
//				Double value = (Double) doc.get(key);
//				System.out.println(key + "====" + value);
			}
			Set centSet = entry.getValue().keySet();
			for(Iterator iter = centSet.iterator(); iter.hasNext();){
				centLength += Math.pow(entry.getValue().get(iter.next()), 2.0);
			}
			
//			for (long i = 0; i < dictSize; i++) {
//				if (entry.getValue().containsKey(i)) {
//					centLength += Math.pow(entry.getValue().get(i), 2.0);//计算中心点向量长度
//					if (doc.containsKey(i))
//						innerProduct += entry.getValue().get(i) * doc.get(i);//计算文档向量和中心点向量的内积
//				}
//			}
			
			//计算余弦距离并更新最近中心点内积
			centLength = Math.sqrt(centLength);
			if (innerProduct / (docLength * centLength) > nearestDistance){
				nearestDistance = innerProduct / (docLength * centLength);
				nearestCendroid = entry.getKey();
			}
			centLength = 0;
			innerProduct = 0;
		}

		return nearestCendroid;
	}
}
