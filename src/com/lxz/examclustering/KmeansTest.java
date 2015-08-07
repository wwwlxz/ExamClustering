package com.lxz.examclustering;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class KmeansTest {
	private static final String CEN_PATH = "/usr/shen/chinesewebkmeans/center/centroid.list";
	
	public static class LastKmeansMapper extends
			Mapper<IntWritable, Text, IntWritable, IntWritable> {
		private static Map<Integer, Map<String, Double>> centers = new HashMap<Integer, Map<String,Double>>();//获取中心点向量
		//private static Map<String, Long> dictWords = new HashMap<String, Long>();//获取词表
		private IntWritable classCenter;//中心点编号
		
		/*
		 * 将文档中心从hdfs中加载至内存
		 */
		protected void setup(Context context) throws IOException, InterruptedException{
			Configuration conf = context.getConfiguration();
			Path centroids = new Path(conf.get("CENPATH"));
//			Path dictPath = new Path(conf.get("DICTPATH"));
			FileSystem fs = FileSystem.get(conf);
			
			SequenceFile.Reader reader = new SequenceFile.Reader(fs, centroids,
					conf);
			IntWritable key = new IntWritable();
			Text value = new Text();
			while (reader.next(key, value)) {
				Map<String, Double> tfidfAndIndex = new HashMap<String, Double>();
				String[] iat = value.toString().split("/");
				for (String string : iat) {
					tfidfAndIndex.put(string.split(":")[0],
							Double.parseDouble(string.split(":")[1]));
//					tfidfAndIndex.put(Long.parseLong(string.split(":")[0]),
//							Double.parseDouble(string.split(":")[1]));
				}
				centers.put(key.get(), tfidfAndIndex);
			}
			reader.close();

			//读取词表
//			SequenceFile.Reader reader1 = new SequenceFile.Reader(fs, dictPath,
//					conf);
//			Text key1 = new Text();
//			LongWritable value1 = new LongWritable();
//			while (reader1.next(key1, value1)) {
//				dictWords.put(key1.toString(), value1.get());
//			}
//			reader1.close();

			super.setup(context);
		}
		
		/**
		 * 计算当前文档与所有文档中心之间的距离，选择最近的中心作为新的文档中心
		 * 
		 * @param context
		 *            输入 key:旧的文档中心 value:当前文档 输出 key:重新选择的文档中心 value:当前文档
		 */
		@Override
		protected void map(IntWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			int nearestNum = 0;//距离最近的中心点的编号
			
			//对原始数据进行分割，取出题目编号，单词1：TFIDF值，单词2：TFIDF值
			//1、取出题目编号
			String[] strs = value.toString().split("\t");
			key = new IntWritable(Integer.parseInt(strs[0].trim()));//取出题目编号
			
			Map<String, Double> doc = null;
			Pattern p = Pattern.compile("\"([^\"]+)\":([^,]+)");//正则匹配取出：单词和TFIDF
			Matcher m = p.matcher(value.toString());
			strs = null;
			while(m.find()){
				strs = m.group().split(":");
				if(strs.length == 2){
					doc = new HashMap<String, Double>();
					doc.put(strs[0].replaceAll("\"", "").trim(), Double.parseDouble(strs[1].trim()
							.replaceAll("\"", "")));
				}
			}
			nearestNum = DocToolTest.returnNearestCentNum(doc, centers);
			this.classCenter = new IntWritable(nearestNum);
			context.write(key, this.classCenter);
		}
	}

	public static class LastKmeansReducer extends
			Reducer<IntWritable, IntWritable, LongWritable, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(IntWritable key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			super.reduce(key, values, context);
		}
	}

	public static void main(String[] args) throws Exception {
		// Add these statements. XXX
		File jarFile = EJob.createTempJar("bin");
		EJob.addClasspath("/home/hadoop/hadoop-1.0.0/conf");
		ClassLoader classLoader = EJob.getClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "192.168.192.100:9001");
		// String[] ars=new String[]{"input","newout"};
		String[] ars = new String[] {
				"hdfs://192.168.192.100:9000/user/root/input",
				"hdfs://192.168.192.100:9000/user/root/output" };
		String[] otherArgs = new GenericOptionsParser(conf, ars)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}

		Job job = new Job(conf, "word count");// 新建MapReduce作业
		// And add this statement. XXX
		((JobConf) job.getConfiguration()).setJar(jarFile.toString());

		job.setJarByClass(KmeansTest.class);// 设置作业启动类
		job.setMapperClass(LastKmeansMapper.class);
		//job.setCombinerClass(LastKmeansReducer.class);
		job.setReducerClass(LastKmeansReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
