package com.lxz.exam.simpleone;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/*
 * 计算每个类型所对应的题目编号
 * 输出为：题目类型	题目编号@题目编号@题目编号
 */
public class RevertTypes {
	public static class RevertTypesMapper extends Mapper<Object, Text, Text, Text>{
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			String[] strs = value.toString().split("\t");
			if(strs.length == 2){
				context.write(new Text(strs[1]), new Text(strs[0]));
			}
		}
	}
	
	public static class RevertTypesReducer extends Reducer<Text, Text, Text, Text>{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			String str = "";
			for(Text val : values){
				str = str + val.toString() + "@";
			}
			context.write(key, new Text(str));
		}
	}
	
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "10.161.214.116:9001");
		String[] ars = new String[]{"hdfs://10.161.214.116:9000/user/root/input_reverttypes", "hdfs://10.161.214.116:9000/user/root/output_reverttypes"};
		String[] otherArgs = new GenericOptionsParser(conf, ars).getRemainingArgs();
		if(otherArgs.length != 2){
			System.out.println("Usage: RevertTypes");
			System.exit(2);
		}
		
		Job job = new Job(conf, "RevertTypes");
		job.setJarByClass(RevertTypes.class);
		
		job.setMapperClass(RevertTypesMapper.class);
		job.setReducerClass(RevertTypesReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		System.out.println(job.waitForCompletion(true) ? 0 : 1);
	}
}

