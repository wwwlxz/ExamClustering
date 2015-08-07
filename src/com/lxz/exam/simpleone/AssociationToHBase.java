package com.lxz.exam.simpleone;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

public class AssociationToHBase {
	public static class AssociationToHBaseMapper extends Mapper<Object, Text, Text, Text>{
		public void map(Object key, Text value,Context context) throws IOException, InterruptedException{
			String[] strs = value.toString().split("\t");
			if(strs.length == 2){
				context.write(new Text(strs[0]), new Text(strs[1]));
			}
		}
	}
	
	public static class AssociationToHBaseReducer extends TableReducer<Text, Text, NullWritable>{
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException{
			String str = "";
			for(Text val : values){
				str = str + val.toString() + ":";
			}
			//Put 实例化，每个词存一行
			Put put = new Put(Bytes.toBytes(key.toString()));
			//列族为mistakes，列修饰符为count，列值为数目
			put.add(Bytes.toBytes("mistakes"), Bytes.toBytes("count"), Bytes.toBytes(str));
			context.write(NullWritable.get(), put);
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		Configuration conf = new Configuration();
		conf.set("mapred.job.tracker", "10.161.214.116:9001");
		conf.set("hbase.zookeeper.quorum", "10.161.214.116");
		conf.set("hbase.zookeeper.property.clientPort", "2161");
		conf.set(TableOutputFormat.OUTPUT_TABLE, "types");
		
		Job job = new Job(conf, "Association to HBase");
		job.setJarByClass(AssociationToHBase.class);
		
		job.setMapperClass(AssociationToHBaseMapper.class);
		job.setReducerClass(AssociationToHBaseReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TableOutputFormat.class);
		
		FileInputFormat.addInputPath(job, new Path("hdfs://10.161.214.116:9000/user/root/input"));
		System.out.println(job.waitForCompletion(true)? 0 : 1);
	}
}
