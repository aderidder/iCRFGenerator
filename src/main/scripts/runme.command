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

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
cd "$SCRIPTPATH"
xattr -cr * > /dev/null 2>&1
java-runtime/bin/java --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED -Dfile.encoding=UTF-8 -cp iCRFGenerator.jar:./lib/* icrfgenerator.CRFGenerator
