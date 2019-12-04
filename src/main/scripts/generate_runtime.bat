@echo off
REM Copyright (c) 2019 VUmc/KWF TraIT2Health-RI
REM
REM This file is part of iCRFGenerator
REM
REM iCRFGenerator is free software: you can redistribute it and/or modify
REM it under the terms of the GNU General Public License as published by
REM the Free Software Foundation, either version 3 of the License, or
REM (at your option) any later version.
REM
REM iCRFGenerator is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM GNU General Public License for more details.
REM
REM You should have received a copy of the GNU General Public License
REM along with iCRFGenerator. If not, see <http://www.gnu.org/licenses/>
@echo on
%1\jlink --module-path=%2 --add-modules=java.base,java.desktop,java.management,java.naming,java.rmi,java.scripting,java.sql,java.xml.crypto,javafx.media,javafx.web,jdk.javadoc --no-man-pages --compress 2 --no-header-files --strip-debug --output %3\java-runtime
copy %4\runme.bat %3
