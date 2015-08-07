package com.lxz.exam.simpleone;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.json.JSONException;
import org.json.JSONObject;
/*
 * step2
 * 判断每道题目所属的类型
 * 输出为：题目编号 题目类型号
 */
public class Types {
	public static class TypesMapper extends Mapper<Object, Text, IntWritable, Text>{
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			String[] strs = value.toString().split("\\{%QUES%\\}");//extract the json
			for(String str : strs){//
				if(str.trim().length() != 0){
					try {
						JSONObject dataJson = new JSONObject(str);
						JSONObject attrs = dataJson.getJSONObject("attrs");
						String knowledge_point_name = (String)attrs.get("knowledge_point_name");
						int id = (int)dataJson.get("id");
						context.write(new IntWritable(id), new Text(knowledge_point_name));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static class TypesReducer extends Reducer<IntWritable, Text, IntWritable, Text>{
		public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			String str = "";
			for(Text val : values){
				str = val.toString();
			}
			context.write(key, new Text(str));
		}
	}
	
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "10.161.214.116:9001");
		String[] ars = new String[]{"hdfs://10.161.214.116:9000/user/root/input_types", "hdfs://10.161.214.116:9000/user/root/output_types"};
		String[] otherArgs = new GenericOptionsParser(conf, ars).getRemainingArgs();
		if(otherArgs.length != 2){
			System.out.println("Usage: Types ");
			System.exit(2);
		}
		
		Job job = new Job(conf, "Types");
		job.setJarByClass(Types.class);
		
		job.setMapperClass(TypesMapper.class);
		job.setReducerClass(TypesReducer.class);
		
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		System.out.println(job.waitForCompletion(true) ? 0 : 1);
	}
}
