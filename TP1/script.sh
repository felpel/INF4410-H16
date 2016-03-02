#!/bin/bash

echo "script starts"

> results.txt

END=7
for ((i=1;i<=END;i++)); do
	echo $i
	echo "Exponent: $i" >> results.txt
    ./client $i 54.153.69.164 >> results.txt
done

echo "script ends"
cat results.txt
