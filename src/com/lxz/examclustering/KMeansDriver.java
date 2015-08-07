package com.lxz.examclustering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class KMeansDriver {

	public class KMeansMapper extends Mapper<Object, Text, IntWritable, Text> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();//读取一行数据
			String[] fields = line.split(" ");//数据按空格划分
			List<ArrayList<Float>> centers = Assistance.getCenters(context
					.getConfiguration().get("centerpath"));//读取聚类中心值
			int k = Integer.parseInt(context.getConfiguration().get("kpath"));//读取聚类点个数k
			float minDist = Float.MAX_VALUE;
			int centerIndex = k;
			// 计算样本点到各个中心的距离，并把样本聚类到距离最近的中心点所属的类
			for (int i = 0; i < k; ++i) {
				float currentDist = 0;
				for (int j = 0; j < fields.length; ++j) {
					float tmp = Math.abs(centers.get(i).get(j + 1)
							- Float.parseFloat(fields[j]));
					currentDist += Math.pow(tmp, 2);
				}
				if (minDist > currentDist) {
					minDist = currentDist;
					centerIndex = i;
				}
			}
			context.write(new IntWritable(centerIndex), new Text(value));
		}
	}

	public class KMeansReducer extends
			Reducer<IntWritable, Text, IntWritable, Text> {

		public void reduce(IntWritable key, Iterable<Text> value,
				Context context) throws IOException, InterruptedException {
			List<ArrayList<Float>> assistList = new ArrayList<ArrayList<Float>>();
			String tmpResult = "";
			for (Text val : value) {
				String line = val.toString();
				String[] fields = line.split(" ");
				List<Float> tmpList = new ArrayList<Float>();
				for (int i = 0; i < fields.length; ++i) {
					tmpList.add(Float.parseFloat(fields[i]));
				}
				assistList.add((ArrayList<Float>) tmpList);
			}
			// 计算新的聚类中心
			for (int i = 0; i < assistList.get(0).size(); ++i) {
				float sum = 0;
				for (int j = 0; j < assistList.size(); ++j) {
					sum += assistList.get(j).get(i);
				}
				float tmp = sum / assistList.size();
				if (i == 0) {
					tmpResult += tmp;
				} else {
					tmpResult += " " + tmp;
				}
			}
			Text result = new Text(tmpResult);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		int repeated = 0;
		/*
		 * 不断提交MapReduce作业指导相邻两次迭代聚类中心的距离小于阈值或到达设定的迭代次数
		 */
		do {
			Configuration conf = new Configuration();
			String[] otherArgs = new GenericOptionsParser(conf, args)
					.getRemainingArgs();
			if (otherArgs.length != 6) {
				System.err
						.println("Usage: <int> <out> <oldcenters> <newcenters> <k> <threshold>");
				System.exit(2);
			}
			conf.set("centerpath", otherArgs[2]);//oldcenters
			conf.set("kpath", otherArgs[4]);//k
			Job job = new Job(conf, "KMeansCluster");// 新建MapReduce作业
			job.setJarByClass(KMeansDriver.class);// 设置作业启动类

			Path in = new Path(otherArgs[0]);
			Path out = new Path(otherArgs[1]);
			FileInputFormat.addInputPath(job, in);// 设置输入路径
			FileSystem fs = FileSystem.get(conf);
			if (fs.exists(out)) {// 如果输出路径存在，则先删除之
				fs.delete(out, true);
			}
			FileOutputFormat.setOutputPath(job, out);// 设置输出路径

			job.setMapperClass(KMeansMapper.class);// 设置Map类
			job.setReducerClass(KMeansReducer.class);// 设置Reduce类

			job.setOutputKeyClass(IntWritable.class);// 设置输出键的类
			job.setOutputValueClass(Text.class);// 设置输出值的类

			job.waitForCompletion(true);// 启动作业

			++repeated;
			System.out.println("We have repeated " + repeated + " times.");
		} while (repeated < 10
				&& (Assistance.isFinished(args[2], args[3],
						Integer.parseInt(args[4]), Float.parseFloat(args[5])) == false));
		// 根据最终得到的聚类中心对数据集进行聚类
		Cluster(args);
	}

	public static void Cluster(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 6) {
			System.err
					.println("Usage: <int> <out> <oldcenters> <newcenters> <k> <threshold>");
			System.exit(2);
		}
		conf.set("centerpath", otherArgs[2]);
		conf.set("kpath", otherArgs[4]);
		Job job = new Job(conf, "KMeansCluster");
		job.setJarByClass(KMeansDriver.class);

		Path in = new Path(otherArgs[0]);
		Path out = new Path(otherArgs[1]);
		FileInputFormat.addInputPath(job, in);
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(out)) {
			fs.delete(out, true);
		}
		FileOutputFormat.setOutputPath(job, out);

		// 因为只是将样本点聚类，不需要reduce操作，故不设置Reduce类
		job.setMapperClass(KMeansMapper.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		job.waitForCompletion(true);
	}
}
