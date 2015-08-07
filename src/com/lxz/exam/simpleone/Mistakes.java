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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

/* step1
 * 获取每道题目出错的次数
 * 读取数据的格式：
 * 2277760	{"64157":{"qid":"64157","kid":"392","answer":"B","is_right":"-1"},"64163":{"qid":"64163","kid":"392","answer":"B","is_right":"-1"}}
 * 输出格式：
 * 错题编号	出现次数
 * 错题编号	出现次数
 */
public class Mistakes {
	public static class MistakesMapper extends Mapper<Object, Text, Text, IntWritable>{
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			String strs = value.toString();
			Pattern p = Pattern.compile("\"qid\":\"[^,]*");
			Matcher m = p.matcher(strs);
			while(m.find()){
				String[] mistake = m.group().replace("\"", "").trim().split(":");
				context.write(new Text(mistake[1]), new IntWritable(1));
			}
		}
	}
	
	public static class MistakesReducer extends Reducer<Text, IntWritable, Text ,IntWritable>{
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
			int sum = 0;
			for(IntWritable val : values){
				sum = sum + val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}
	
	public static void main(String[] args) throws Exception{
		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "10.161.214.116:9001");
		String[] ars = new String[]{"hdfs://10.161.214.116:9000/user/root/input_mistakes", "hdfs://10.161.214.116:9000/user/root/output_mistakes"};
		String[] otherArgs = new GenericOptionsParser(conf, ars).getRemainingArgs();
		if(otherArgs.length != 2){
			System.out.println("Usage: Mistakes	");
			System.exit(2);
		}
		Job job = new Job(conf, "Mistakes");
		
		job.setJarByClass(Mistakes.class);
		
		job.setMapperClass(MistakesMapper.class);
		job.setReducerClass(MistakesReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
