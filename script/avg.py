import os,sys

s = 0
cnt = 0
with open("compress_rate.txt") as f:
	for line in f:
		l = line[:-1]
		s += int(l)
		cnt += 1

f1 = open("compree_avg_rate.txt","w+")
f1.write(str(s/cnt)+"\n")