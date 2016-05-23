#!/usr/bin/python

import subprocess
import sys
import re
def correctURL(mystr):
	link_start = mystr.rfind('/')
	new_link = mystr[link_start+1:]
	
	last_dot = new_link.rfind('.')
	temp_link = new_link[:last_dot]
	prev_dot = temp_link.rfind('.')
	tail = new_link[prev_dot+1:]
	new_link = new_link[:prev_dot] + '/' + tail
	
	return new_link
def isURL(mystr):
	if (".html"==mystr.strip()[-5:] or ".htm"==mystr.strip()[-4:]):
		return True
	else:
		return False
def getResults(mystr):
	#myfile = open("cleantextdata","r");
	#str = ""
	#for line in myfile:
	#	str+=line
	#myfile.close()
	#print str	
	
	#m = re.search(r"LINGO SELECETED",str)
	#print m.start(1)
	lingo_start = "RESULT USING LINGO"
	lingo_start_idx = mystr.find(lingo_start)
	lingo_end = "LINGO-CLUSTERING-END"
	lingo_end_idx = mystr.find(lingo_end)

	#print lingo_start_idx," ",lingo_end_idx
	lingo_part = mystr[lingo_start_idx:lingo_end_idx]
	#print lingo_part


	kmeans_start = "RESULT USING KMEANS"
        kmeans_start_idx = mystr.find(kmeans_start)
        kmeans_end = "KMEANS-CLUSTERING-END"
        kmeans_end_idx = mystr.find(kmeans_end)

        #print kmeans_start_idx," ",kmeans_end_idx
        kmeans_part = mystr[kmeans_start_idx:kmeans_end_idx]
        #print kmeans_part


	stc_start = "RESULT USING STC"
        stc_start_idx = mystr.find(stc_start)
        stc_end = "STC-CLUSTERING-END"
        stc_end_idx = mystr.find(stc_end)

        #print stc_start_idx," ",stc_end_idx
        stc_part = mystr[stc_start_idx:stc_end_idx]
        #print stc_part
	return (lingo_part, kmeans_part, stc_part)

#if __name__ == "__main__":
#	main()
