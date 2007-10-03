
// reference a stream in a BP environment

// normal behavior is to set the environment when creating the stream
// however -- sometimes you might need to make the stream outside the environment
// in that case you can pass in an environment
// if the envir arg to *new is nil, we set the environment at stream time

BPStream : Pattern {
	var	<>key, <>reset, <>envir;
	*new { |key, reset = false, envir|
		^super.newCopyArgs(key, reset, envir)
	}
	
	asStream {
		var	streamKey = (key ++ "Stream").asSymbol;
		envir ?? { envir = currentEnvironment; };
		(reset or: { envir[streamKey].isNil }).if({
			envir.use({
				streamKey.envirPut(key.envirGet.asStream);
				streamKey.envirGet.isNil.if({
					"Source stream for BPStream(%) is not populated."
						.format(key.asCompileString).warn;
				});
			});
		});
		^FuncStream({ |inval|
//streamKey.envirGet.debug(streamKey).next(inval).debug("output");
			streamKey.envirGet.next(inval)
		}).envir_(envir)   // by default FuncStreamEnvir assigns the current envir
	}
}

// Proutine, but protects against nil being passed in
// intended for routines that yield events
// this is a workaround for EventStreamPlayer-stop
Prt : Prout {
	asStream {
		var	stream = super.asStream;
		
		^FuncStream({ |inval|
			inval.notNil.if({ stream.next(inval) });
		}, { stream.reset })
	}
}
