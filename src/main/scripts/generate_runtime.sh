#!/bin/bash
# Copyright (c) 2019 VUmc/KWF TraIT2Health-RI
#
# This file is part of iCRFGenerator
#
# iCRFGenerator is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# iCRFGenerator is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with iCRFGenerator. If not, see <http://www.gnu.org/licenses/>

$1/jlink --module-path=$2 --add-modules=java.base,java.desktop,java.management,java.naming,java.rmi,java.scripting,java.sql,java.xml.crypto,javafx.media,javafx.web,jdk.javadoc,jdk.crypto.cryptoki --no-man-pages --compress 2 --no-header-files --strip-debug --output $3/java-runtime
cp $4/runme.command $3
