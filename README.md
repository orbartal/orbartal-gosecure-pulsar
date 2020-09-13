# orbartal-gosecure-pulsar
A first demo app of pulsar

1.  Setup a pulsar locally to experiment with using the java client and junit.
2. 	Write a microservice that generate pulsar event by message text and topic name given in rest api
3.  Write a microservice that subscribes to multiple pulsar topics and processes the messages received.
4.  Persist the pulsar messages to disk (initially) in sequential order by topic and timestamp with a storage pattern that allows purging of data by year, month and day.  
	E.g. abcTopic/2020/09/08/20200908-101956_345_messageId.msg and so on allowing deletion of the individual directories.
	Expand the persistence interface to save the data in something besides a filesystem
5.  Write a microservice that publishes to multiple pulsar topics and emits the messages from the aforementioned disk storage in sequential order back onto the topics.

Note: Security is not a requirement at this project