#!/usr/bin/env python

import logging
#from SimpleBackend import SimpleBackend
from SimpleComponent import SimpleComponent, SUBDOMAIN, do_exit, SAVE
import signal

import time
import cProfile
from pwd import getpwnam
import os

import tornado.httpserver
import tornado.httpclient
import tornado.ioloop
import tornado.web

from http import *

ch = logging.StreamHandler()
ch.setLevel(logging.INFO)
formatter = logging.Formatter(
    "%(levelname).1s%(asctime)s %(filename)s:%(lineno)d] %(message)s",
    datefmt='%Y%m%d:%H%M%S')
ch.setFormatter(formatter)
for L in [logging.getLogger(''), logging.getLogger()]:
  L.setLevel(logging.INFO)
  L.addHandler(ch)



import urllib



from sleekxmpp.xmlstream import PROFILERS

def main() :
        
	signal.signal(signal.SIGTERM, do_exit)
	signal.signal(signal.SIGHUP, SAVE)
        oh = OutboundHandler()

	component = SimpleComponent(
		jid = SUBDOMAIN + ".partych.at", password = "secret",
		server = "10.220.227.98", port = 5275, oh = oh)
#		server = "127.0.0.1", port = 5275, oh = oh)

	component.start()
        application = tornado.web.Application([
            (r"/", MainHandler),
            (r"/varz", VarzHandler),
            (r'/___control___', InboundControlHandler, 
             dict(component=component)),
            ])

        application.listen(80)
        data_dir='/etc/certs'
        server = tornado.httpserver.HTTPServer(application,
           ssl_options={"certfile": os.path.join(data_dir, "server.crt"),
           "keyfile": os.path.join(data_dir, "server.key"),
       })
        server.listen(443)
        logging.info('dropping permissions')
        os.setuid(getpwnam('nobody').pw_uid)
        def print_profiles():
          for k,(p,name) in enumerate(PROFILERS):
            logging.info('THREAD STATS dumping stats for thread %s', name)
            p.dump_stats('%s.profile'% name)

        tornado.ioloop.PeriodicCallback(print_profiles, 60000).start()
        def runner(name, target):
          import cProfile
          profiler = cProfile.Profile()
          PROFILERS.append((profiler,name))
          logging.info('THREAD starting  %s',
                       name)
          profiler.runcall(target)
            
        runner('ioloop', tornado.ioloop.IOLoop.instance().start)
        

if __name__ == '__main__' :
  main()

