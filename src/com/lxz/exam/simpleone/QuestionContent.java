package com.lxz.exam.simpleone;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionContent {
	public static class QuestionContentMapper extends Mapper<Object, Text, IntWritable, Text>{
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			String[] strs = value.toString().split("\\{%QUES%\\}");//extract the json
			for(String str : strs){//
				if(str.trim().length() != 0){
					try {
						JSONObject dataJson = new JSONObject(str);
						String content = (String)dataJson.get("content");
						//JSONObject attrs = dataJson.getJSONObject("attrs");
						//String knowledge_point_name = (String)attrs.get("knowledge_point_name");
						int id = (int)dataJson.get("id");
						context.write(new IntWritable(id), new Text(content));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static class QuestionContentReducer extends Reducer<Text, Text, Text, Text>{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			
		}
	}
	
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "10.161.214.116:9001");
		String[] ars = new String[]{"hdfs://10.161.214.116:9000/user/root/input_types", "hdfs://10.161.214.116:9000/user/root/output_questioncontent"};
		String[] otherArgs = new GenericOptionsParser(conf, ars).getRemainingArgs();
		if(otherArgs.length != 2){
			System.out.println("Usage: QuestionContent");
			System.exit(2);
		}
		
		Job job = new Job(conf, "QuestionContent");
		job.setJarByClass(QuestionContent.class);
		
		job.setMapperClass(QuestionContentMapper.class);
		//job.setReducerClass(QuestionContentReducer.class);
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		System.out.println(job.waitForCompletion(true) ? 0 : 1);
	}
}
