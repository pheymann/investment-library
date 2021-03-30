from hseeberger/scala-sbt:11.0.10_1.4.9_2.13.5

COPY ./ /investment/

WORKDIR /investment/

RUN sbt "publishLocal"

RUN sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/com-lihaoyi/Ammonite/releases/download/2.3.8/2.13-2.3.8) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm'

CMD ["/bin/bash"]