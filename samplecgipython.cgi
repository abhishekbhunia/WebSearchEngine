#!/usr/bin/python

import subprocess
import cleantext
import urllib
import cgi
import cgitb

cgitb.enable()

form = cgi.FieldStorage()
print "Content-type: text/html"
print
print "<title>Test CGI</title>"
print "<h2>Clustering Results</h2>"
queryString = form.getvalue('searchQuery')
#print queryString
#queryString = "blue whale"
command = "java -cp \
.:./lib/*:./lib/carrot/required/*:./lib/carrot/optional/* MainApp \
-index /home/ab5966/public_html/cgi-bin/index \
-docs /home/ab5966/public_html/cgi-bin/crawldocs \
-query " + '"%s"'%str(queryString)

p1 = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)

output, errors = p1.communicate()

#print output
(lingo, kmeans, stc) = cleantext.getResults(output)

print "lstartidx" #9
list_lingo = lingo.split('\n')
for item in list_lingo:
	if cleantext.isURL(item) == True:
		new_url = cleantext.correctURL(item)
		print r'<a href="{0}">{1}</a>'.format("http://"
+new_url,"http://"+new_url)
		print "</br>"
	else:
		print item, "</br>"
print "lendidx" #7
#print "kmeans",kmeans

print "kstartidx"
list_kmeans = kmeans.split('\n')
for item in list_kmeans:
        if cleantext.isURL(item) == True:
                new_url = cleantext.correctURL(item)
                print r'<a href="{0}">{1}</a>'.format("http://"
+new_url,"http://"+new_url)
                print "</br>"
        else:
                print item, "</br>"
print "kendidx"

#print "stc",stc
print "sstartidx"
list_stc = stc.split('\n')
for item in list_stc:
	if cleantext.isURL(item) == True:
		new_url = cleantext.correctURL(item)
	        print r'<a href="{0}">{1}</a>'.format("http://"
+new_url,"http://"+new_url)
                print "</br>"
        else:
                print item, "</br>"
print "sendidx"

