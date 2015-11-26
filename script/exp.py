import os,sys


if __name__=="__main__":
	fieldname = sys.argv[1]
	fieldvalue = sys.argv[2]
	with open("workloada","r+") as f:
		lines = []
		for line in f:
			lines.append(line)
		f.truncate()
		for line in lines:
			if fieldname in line:
				line = str(fieldname)+"="+fieldvalue+"\n"
			f.write(line)
